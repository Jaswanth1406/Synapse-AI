from fastapi import FastAPI, Request, HTTPException
from fastapi.exceptions import RequestValidationError
from fastapi.responses import Response
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from pydantic import BaseModel
import httpx
import os
import asyncio
import asyncpg
import json
import uuid
from datetime import datetime, timezone, timedelta
from dateutil import parser
from dotenv import load_dotenv
load_dotenv("../frontend/.env") 
from twilio.rest import Client
from crm_connector import push_unified_event, map_lead_status, fetch_crm_leads

async def analyze_transcript_with_groq(transcript: str) -> dict:
    """
    Uses Groq LLM (llama3-70b) to intelligently extract intent, sentiment,
    lead quality, and engagement from a call transcript.
    """
    groq_api_key = os.environ.get("GROQ_API_KEY")
    if not groq_api_key or transcript == "No transcript generated.":
        return {}

    system_prompt = """
You are a lead intelligence engine. Analyze sales call transcripts and return ONLY a valid JSON object.

Return exactly this structure:
{
  "intent": "INTERESTED" | "NOT_INTERESTED" | "CALLBACK" | "INFO_SEEKING" | "UNKNOWN",
  "lead_status": "HOT" | "WARM" | "COLD",
  "sentiment": "positive" | "neutral" | "negative",
  "callback_requested": true | false,
  "urgency_level": "HIGH" | "MEDIUM" | "LOW",
  "product_interest": "<what course/service they asked about or null>",
  "pain_point": "<main concern the user expressed or null>",
  "next_step": "CALLBACK" | "COUNSELOR_FOLLOWUP" | "DROP" | "CLOSE",
  "summary": "<1-2 sentence summary of the conversation>",
  "engagement_score": <integer 1-10 based on how engaged the user was>
}

Return ONLY the JSON. No markdown, no explanation.
"""

    try:
        async with httpx.AsyncClient(timeout=20.0) as client:
            response = await client.post(
                "https://api.groq.com/openai/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {groq_api_key}",
                    "Content-Type": "application/json"
                },
                json={
                    "model": "meta-llama/llama-4-scout-17b-16e-instruct",
                    "messages": [
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": f"Analyze this transcript:\n\n{transcript[:4000]}"}
                    ],
                    "temperature": 0.1,
                    "max_tokens": 500
                }
            )
            response.raise_for_status()
            raw = response.json()["choices"][0]["message"]["content"].strip()
            # Strip any markdown code block if Groq wraps it
            if raw.startswith("```"):
                raw = raw.split("\n", 1)[1].rsplit("```", 1)[0]
            return json.loads(raw)
    except Exception as e:
        print(f"Groq analysis failed: {e}")
        return {}

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize connection pool to Neon DB
    db_url = os.environ.get("DATABASE_URL")
    if db_url:
        print("Connecting to Neon Database...")
        app.state.pool = await asyncpg.create_pool(db_url)
        # Create Call Transcripts table automatically if missing
        async with app.state.pool.acquire() as conn:
            await conn.execute('''
                CREATE TABLE IF NOT EXISTS call_transcripts (
                    id SERIAL PRIMARY KEY,
                    call_id TEXT UNIQUE,
                    contact_id TEXT,
                    phone_number TEXT,
                    status TEXT,
                    intent TEXT,
                    transcript TEXT,
                    caller_name TEXT,
                    company_name TEXT,
                    designation TEXT,
                    email_provided TEXT,
                    callback_requested BOOLEAN,
                    callback_time TEXT,
                    language_spoken TEXT,
                    sentiment TEXT,
                    call_outcome TEXT,
                    objection_raised BOOLEAN,
                    objection_detail TEXT,
                    product_interest TEXT,
                    budget_mentioned BOOLEAN,
                    budget_range TEXT,
                    urgency_level TEXT,
                    decision_maker BOOLEAN,
                    competitor_mentioned TEXT,
                    pain_point TEXT,
                    next_step TEXT,
                    meeting_date TEXT,
                    call_duration_seconds INTEGER,
                    questions_asked TEXT,
                    referral_source TEXT,
                    do_not_call BOOLEAN DEFAULT FALSE,
                    lead_status TEXT,
                    engagement_score INTEGER,
                    summary TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            ''')
            # Ensure Groq columns exist on older tables (safe migration)
            for col_def in [
                "ALTER TABLE call_transcripts ADD COLUMN IF NOT EXISTS lead_status TEXT",
                "ALTER TABLE call_transcripts ADD COLUMN IF NOT EXISTS engagement_score INTEGER",
                "ALTER TABLE call_transcripts ADD COLUMN IF NOT EXISTS summary TEXT",
                "ALTER TABLE call_transcripts ADD COLUMN IF NOT EXISTS user_id TEXT",
                "ALTER TABLE call_transcripts ADD COLUMN IF NOT EXISTS recording_url TEXT"
            ]:
                try:
                    await conn.execute(col_def)
                except Exception:
                    pass
            # Create Scheduled Calls table
            await conn.execute('''
                CREATE TABLE IF NOT EXISTS scheduled_calls (
                    id TEXT PRIMARY KEY,
                    phone_number TEXT NOT NULL,
                    scheduled_time TIMESTAMP WITH TIME ZONE,
                    language TEXT,
                    retry_count INTEGER,
                    current_attempt INTEGER DEFAULT 0,
                    status TEXT,
                    user_id TEXT,
                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                );
            ''')
            # Ensure user_id exists if table was created older
            await conn.execute("ALTER TABLE scheduled_calls ADD COLUMN IF NOT EXISTS user_id TEXT")
            
        print("Neon Database ready and tables verified.")
        
        # --- STARTUP RECOVERY MODE ---
        print("Checking for interrupted scheduled calls...")
        async with app.state.pool.acquire() as conn:
            pending_calls = await conn.fetch("SELECT * FROM scheduled_calls WHERE status IN ('pending', 'retrying')")
            if pending_calls:
                print(f"Recovering {len(pending_calls)} interrupted calls...")
                for row in pending_calls:
                    # Delay launching slightly to avoid connection storms on boot
                    asyncio.create_task(startup_recovery_task(row))
    else:
        app.state.pool = None
        print("WARNING: DATABASE_URL not found, DB storage disabled.")
    
    yield
    
    # Shutdown gracefully
    if getattr(app.state, "pool", None):
        await app.state.pool.close()

# Forcing auto-reload to recreate Neon DB tables
app = FastAPI(title="Synapse AI - Python Backend", lifespan=lifespan)

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    body = await request.body()
    print("=== 422 VALIDATION ERROR ===")
    print(f"Error Details: {exc.errors()}")
    print(f"Raw Webhook Body: {body.decode()}")
    print("============================")
    return Response(status_code=422, content=str(exc))

# Allow Next.js frontend to make requests without browser CORS errors
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

async def startup_recovery_task(row):
    """Helper to cleanly launch _schedule_call_task on boot."""
    await asyncio.sleep(2) # Give server a moment to finish starting
    print(f"Resuming task for {row['phone_number']} (ID: {row['id']})")
    # _schedule_call_task defined below will be available at runtime
    await _schedule_call_task(
        schedule_id=row['id'], 
        phone=row['phone_number'], 
        scheduled_time=row['scheduled_time'].isoformat(), 
        language=row['language'], 
        max_attempts=row['retry_count']
    )

# Maps call_id -> schedule entry for webhook-based retry detection
call_retry_registry: dict[str, dict] = {}
# Maps call_id -> asyncio.Event for unified waiting
active_call_events: dict[str, asyncio.Event] = {}

class ScheduleCallPayload(BaseModel):
    phone_number: str
    scheduled_time: str
    language: str | None = "english"
    retry_count: int | None = 3  # This is actually Max Attempts from the UI now
    user_id: str | None = None  # Optional in payload, preferred in headers

def get_user_id(request: Request) -> str:
    # Use headers for web app, fallback to query params for CSV, then 'anonymous'
    return (
        request.headers.get("X-User-ID") or 
        request.query_params.get("x_user_id") or 
        request.query_params.get("user_id") or 
        "anonymous"
    )

# A simple POST endpoint for Dograh AI to send call completion webhooks
@app.post("/api/webhooks/dograh")
async def handle_dograh_webhook(request: Request):
    # Dynamically accept ANY JSON Dograh sends so we never crash with 422
    payload = await request.json()
    
    # HIGH VISIBILITY LOGGING for debugging retry logic
    print("\n" + "="*50)
    print("🚀 DOGRAH WEBHOOK RECEIVED")
    print(json.dumps(payload, indent=2))
    print("="*50 + "\n")

    # Detection of call_id using multiple possible keys Dograh might use
    call_id = str(
        payload.get("call_id") or 
        payload.get("workflow_run_id") or 
        payload.get("id") or 
        "unknown_id"
    )
    status = payload.get("status", "completed")
    call_duration = int(payload.get("duration", 0) or 0)
    recording_url = payload.get("recording_url") or payload.get("audio_url")
    
    print(f"Received Webhook from Dograh for call: {call_id}")
    
    # Dograh sends URLs instead of text! We must download the transcript!
    transcript_text = "No transcript generated."
    transcript_url = payload.get("transcript_url")
    if transcript_url:
        try:
            api_key = os.environ.get("NEXT_PUBLIC_DOGRAH_API_KEY", "")
            async with httpx.AsyncClient() as client:
                t_resp = await client.get(transcript_url, headers={"X-API-Key": api_key}, follow_redirects=True)
                if t_resp.status_code == 200:
                    transcript_text = t_resp.text
                else:
                    print(f"Failed to download transcript! Status: {t_resp.status_code}, Response: {t_resp.text}")
        except Exception as e:
            print(f"Warning: Failed to fetch transcript from Dograh URL: {e}")
            
    print(f"Transcript Snippet: {transcript_text[:50]}...")

    # === GROQ AI INTENT ANALYSIS ===
    print("Running Groq AI intent analysis...")
    groq_analysis = await analyze_transcript_with_groq(transcript_text)
    print(f"Groq Analysis Result: {groq_analysis}")

    # Merge Groq analysis with Dograh payload fields (Groq takes priority)
    intent = groq_analysis.get("intent") or payload.get("intent", "UNKNOWN")
    sentiment = groq_analysis.get("sentiment") or payload.get("sentiment")
    lead_status = groq_analysis.get("lead_status")
    urgency_level = groq_analysis.get("urgency_level") or payload.get("urgency_level")
    product_interest = groq_analysis.get("product_interest") or payload.get("product_interest")
    pain_point = groq_analysis.get("pain_point") or payload.get("pain_point")
    next_step = groq_analysis.get("next_step") or payload.get("next_step")
    callback_requested = groq_analysis.get("callback_requested", False)
    engagement_score = groq_analysis.get("engagement_score")
    summary = groq_analysis.get("summary")

    contact_id = payload.get("contact_id")
    
    pool = getattr(request.app.state, "pool", None)
    if pool:
        try:
            async with pool.acquire() as conn:
                # Add new groq columns if they don't exist (safe migration)
                for col_def in [
                    "ALTER TABLE call_transcripts ADD COLUMN IF NOT EXISTS lead_status TEXT",
                    "ALTER TABLE call_transcripts ADD COLUMN IF NOT EXISTS engagement_score INTEGER",
                    "ALTER TABLE call_transcripts ADD COLUMN IF NOT EXISTS summary TEXT"
                ]:
                    try:
                        await conn.execute(col_def)
                    except Exception:
                        pass

                await conn.execute('''
                    INSERT INTO call_transcripts (
                        call_id, contact_id, phone_number, status, intent, transcript,
                        caller_name, company_name, designation, email_provided,
                        callback_requested, callback_time, language_spoken, sentiment,
                        call_outcome, objection_raised, objection_detail, product_interest,
                        budget_mentioned, budget_range, urgency_level, decision_maker,
                        competitor_mentioned, pain_point, next_step, meeting_date,
                        call_duration_seconds, questions_asked, referral_source, do_not_call,
                        lead_status, engagement_score, summary, user_id, recording_url
                    )
                    VALUES (
                        $1, $2, $3, $4, $5, $6,
                        $7, $8, $9, $10,
                        $11, $12, $13, $14,
                        $15, $16, $17, $18,
                        $19, $20, $21, $22,
                        $23, $24, $25, $26,
                        $27, $28, $29, $30,
                        $31, $32, $33, $34, $35
                    )
                    ON CONFLICT (call_id) DO UPDATE SET
                        status = EXCLUDED.status,
                        intent = EXCLUDED.intent,
                        transcript = EXCLUDED.transcript,
                        sentiment = EXCLUDED.sentiment,
                        urgency_level = EXCLUDED.urgency_level,
                        product_interest = EXCLUDED.product_interest,
                        pain_point = EXCLUDED.pain_point,
                        next_step = EXCLUDED.next_step,
                        callback_requested = EXCLUDED.callback_requested,
                        lead_status = EXCLUDED.lead_status,
                        engagement_score = EXCLUDED.engagement_score,
                        summary = EXCLUDED.summary,
                        caller_name = EXCLUDED.caller_name,
                        company_name = EXCLUDED.company_name,
                        call_duration_seconds = EXCLUDED.call_duration_seconds,
                        do_not_call = EXCLUDED.do_not_call,
                        recording_url = COALESCE(EXCLUDED.recording_url, call_transcripts.recording_url)
                ''',
                call_id, contact_id, payload.get("phone_number"), status, intent, transcript_text,
                payload.get("caller_name"), payload.get("company_name"), payload.get("designation"), payload.get("email_provided"),
                callback_requested, payload.get("callback_time"), payload.get("language_spoken"), sentiment,
                payload.get("call_outcome"), str(payload.get("objection_raised")).lower() == 'true', payload.get("objection_detail"), product_interest,
                str(payload.get("budget_mentioned")).lower() == 'true', payload.get("budget_range"), urgency_level, str(payload.get("decision_maker")).lower() == 'true',
                payload.get("competitor_mentioned"), pain_point, next_step, payload.get("meeting_date"),
                call_duration, payload.get("questions_asked"), payload.get("referral_source"), str(payload.get("do_not_call")).lower() == 'true',
                lead_status, engagement_score, summary, payload.get("user_id", "anonymous"), recording_url
                )
            print("Successfully stored Groq-enriched transcript in Neon Database.")
        except Exception as e:
            print(f"Neon DB Error: Failed to save transcript - {e}")

    # Map internal status to ESPO CRM status using crm_connector
    if contact_id:
        internal_status = f"CALL_{intent.upper()}"
        espo_status = map_lead_status(internal_status)
        update_data = {
            "description": transcript_text,
            "status": espo_status
        }
        await push_unified_event(
            lead={"id": contact_id},
            event_type="LEAD_UPDATE",
            data=update_data
        )
    
    print(f"WEBHOOK DEBUG: call_id={call_id}, duration={call_duration}, payload_keys={list(payload.keys())}")
    
    # === Signal the active task that the webhook arrived ===
    if call_id in active_call_events:
        print(f"Signaling event for call_id: {call_id}")
        # Store latest data in registry for the task to read
        if call_id in call_retry_registry:
            call_retry_registry[call_id]["last_webhook_payload"] = payload
            call_retry_registry[call_id]["last_duration"] = call_duration
            call_retry_registry[call_id]["last_status"] = status
        active_call_events[call_id].set()
    
    return {"message": "Webhook processed successfully", "detected_intent": intent}


def _analyze_intent(transcript: str | None) -> str:
    if not transcript:
        return "unknown"
    transcript_lower = transcript.lower()
    if "not interested" in transcript_lower or "stop calling" in transcript_lower:
        return "not_interested"
    if "send me" in transcript_lower or "more info" in transcript_lower or "yes" in transcript_lower:
        return "interested"
    return "follow_up"



RETRY_DELAY_SECONDS = 60  # Wait 60s between automatic retries
SHORT_CALL_THRESHOLD_SECONDS = 12  # Calls shorter than this are treated as "cut"

async def _trigger_one_call(phone_number: str, language: str, lead_name: str, use_webhook: bool, webhook_url: str, api_key: str, agent_id: str) -> str:
    """Fires a single call. Returns call_id on success, raises on failure."""
    import uuid
    async with httpx.AsyncClient(timeout=20.0) as client:
        if use_webhook:
            response = await client.post(
                webhook_url,
                headers={"Content-Type": "application/json"},
                json={"phone_number": phone_number, "lead_name": lead_name, "lead_id": "undefined"}
            )
        else:
            if not api_key:
                raise Exception("Dograh API key not configured")
            dograh_url = f"https://api.dograh.com/api/v1/public/agent/{agent_id}"
            # Use BACKEND_PUBLIC_URL from .env to tell Dograh where to send the webhook
            backend_public_url = os.environ.get("BACKEND_PUBLIC_URL", "").rstrip("/")
            webhook_callback = f"{backend_public_url}/api/webhooks/dograh" if backend_public_url else None
            
            payload_data = {
                "phone_number": phone_number,
                "initial_context": {"language": language, "lead_name": lead_name}
            }
            if webhook_callback:
                payload_data["webhook_url"] = webhook_callback
                print(f"[Dograh] Setting webhook callback to: {webhook_callback}")

            response = await client.post(
                dograh_url,
                headers={"X-API-Key": api_key, "Content-Type": "application/json"},
                json=payload_data
            )
        response.raise_for_status()
        try:
            data = response.json()
            print(f"DEBUG: Dograh Full Response: {json.dumps(data, indent=2)}")
        except:
            data = {"message": response.text}
            print(f"DEBUG: Dograh Raw Response: {response.text}")
        return str(data.get("workflow_run_id") or data.get("call_id") or data.get("id") or uuid.uuid4())


async def update_schedule_status(schedule_id: str, new_status: str, new_attempt: int = None):
    """Helper to update scheduled call status in DB"""
    pool = getattr(app.state, "pool", None)
    if pool:
        try:
            async with pool.acquire() as conn:
                if new_attempt is not None:
                    await conn.execute("UPDATE scheduled_calls SET status = $1, current_attempt = $2 WHERE id = $3", new_status, new_attempt, schedule_id)
                else:
                    await conn.execute("UPDATE scheduled_calls SET status = $1 WHERE id = $2", new_status, schedule_id)
        except Exception as e:
            print(f"Failed to update schedule status in DB: {e}")

async def _schedule_call_task(schedule_id: str, phone: str, scheduled_time: str, language: str, max_attempts: int, user_id: str = "anonymous"):
    """Handles scheduling, retry loop, and status updates for a scheduled call using Neon DB."""
    use_webhook = os.environ.get("USE_WEBHOOK", "false").lower() == "true"
    webhook_url = os.environ.get("TRIGGER_WEBHOOK_URL", "")
    api_key = os.environ.get("NEXT_PUBLIC_DOGRAH_API_KEY", "")
    agent_id = os.environ.get("DOGRAH_AGENT_ID", "af96de66-753e-4201-b166-ce5eccab3951")
    pool = getattr(app.state, "pool", None)

    try:
        # --- Sleep until scheduled time ---
        try:
            ist_tz = timezone(timedelta(hours=5, minutes=30))
            now_ist = datetime.now(ist_tz)
            sched_raw = scheduled_time.rstrip('Z')
            if '+' in sched_raw:
                sched_raw = sched_raw[:sched_raw.rfind('+')]
            target_time = parser.parse(sched_raw).replace(tzinfo=ist_tz)
            delay = (target_time - now_ist).total_seconds()
            if delay > 0:
                print(f"[Schedule] Call to {phone} firing in {int(delay)}s (IST: {target_time.strftime('%H:%M:%S')})")
                await asyncio.sleep(delay)
            else:
                print(f"[Schedule] Scheduled time is in the past ({int(-delay)}s). Firing immediately.")
        except Exception as e:
            print(f"[Schedule] Could not parse time '{scheduled_time}'. Firing immediately. Error: {e}")

        # --- Retry Loop ---
        attempt = 0

        while attempt < max_attempts:
            # Check if cancelled in DB
            db_status = None
            if pool:
                async with pool.acquire() as conn:
                    db_status = await conn.fetchval("SELECT status FROM scheduled_calls WHERE id = $1", schedule_id)
            if db_status == "cancelled":
                print(f"[Retry] Schedule {schedule_id} was cancelled in DB. Stopping.")
                return

            attempt_label = f"Attempt {attempt + 1}/{max_attempts}"
            print(f"[{attempt_label}] Triggering call to {phone}...")
            
            await update_schedule_status(schedule_id, "in_progress", attempt + 1)

            try:
                call_id = await _trigger_one_call(
                    phone, language or "en",
                    "Scheduled Lead", use_webhook, webhook_url, api_key, agent_id
                )
                print(f"[{attempt_label}] Call dispatched. call_id={call_id}")

                # Register for webhook-based short-call detection
                call_retry_registry[call_id] = {
                    "schedule_id": schedule_id,
                    "phone": phone,
                    "attempt": attempt,
                    "max_retries": max_attempts,
                    "last_duration": -1,
                    "last_status": "in_progress"
                }
                
                event = asyncio.Event()
                active_call_events[call_id] = event

                # Save to DB (with timeout to prevent stalling)
                if pool:
                    try:
                        print(f"[{attempt_label}] Saving call reference to database...")
                        async def _save():
                            async with pool.acquire() as conn:
                                await conn.execute('''
                                    INSERT INTO call_transcripts (call_id, phone_number, status, intent, transcript, language_spoken, caller_name, user_id)
                                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                                    ON CONFLICT (call_id) DO NOTHING
                                ''', call_id, phone, "queued", "pending",
                                   f"[{attempt_label}] Scheduled call fired at {scheduled_time}.",
                                   language or "en", "Scheduled Lead", user_id)
                        await asyncio.wait_for(_save(), timeout=7.0)
                    except Exception as db_err:
                        print(f"[{attempt_label}] DB timeout or error: {db_err}. Continuing to polling regardless.")

                # --- Wait for completion via Webhook ---
                print(f"[{attempt_label}] Call ringing/in-progress. Waiting for Dograh webhook...")
                
                final_status = "unknown"
                final_duration = -1
                call_resolved = False
                
                try:
                    # Give the call up to 3 minutes to complete + webhook to arrive
                    await asyncio.wait_for(event.wait(), timeout=180.0)
                    print(f"[{attempt_label}] Webhook received!")
                    reg_data = call_retry_registry.get(call_id, {})
                    final_status = reg_data.get("last_status", "completed")
                    final_duration = reg_data.get("last_duration", -1)
                    call_resolved = True
                except asyncio.TimeoutError:
                    print(f"[{attempt_label}] Webhook timeout after 3 minutes.")
                
                # Clean up registries
                call_retry_registry.pop(call_id, None)
                active_call_events.pop(call_id, None)
                
                # --- Make Retry Decision ---
                print(f"[{attempt_label}] Deciding next steps: Duration={final_duration}s, Status={final_status}")
                
                needs_retry = False
                if not call_resolved:
                    print(f"[{attempt_label}] No webhook received. Assuming call failed or dropped.")
                    needs_retry = True
                elif final_status in ["no-answer", "busy", "failed", "canceled"]:
                    print(f"[{attempt_label}] No Answer detected (Status: {final_status}).")
                    needs_retry = True
                elif final_duration >= 0 and final_duration < SHORT_CALL_THRESHOLD_SECONDS:
                    print(f"[{attempt_label}] Short Call detected (Duration: {final_duration}s < {SHORT_CALL_THRESHOLD_SECONDS}s).")
                    needs_retry = True
                    
                if needs_retry:
                    attempt += 1
                    if attempt < max_attempts:
                        print(f"[Retry] Scheduling retry {attempt + 1}/{max_attempts} in {RETRY_DELAY_SECONDS}s...")
                        await update_schedule_status(schedule_id, f"retrying", attempt)
                        await asyncio.sleep(RETRY_DELAY_SECONDS)
                        continue # Loops back to trigger the next call
                    else:
                        print(f"[Retry] Max attempts ({max_attempts}) reached. Failing schedule.")
                        await update_schedule_status(schedule_id, "failed", attempt)
                        return # Exit task
                else:
                    print(f"[{attempt_label}] Normal call completed successfully ({final_duration}s).")
                    await update_schedule_status(schedule_id, "completed", attempt)
                    return # Exit task

            except Exception as e:
                print(f"[{attempt_label}] Failed: {e}")
                attempt += 1
                if attempt < max_attempts:
                    print(f"[Retry] Waiting {RETRY_DELAY_SECONDS}s before retry {attempt + 1}...")
                    await update_schedule_status(schedule_id, f"retrying", attempt)
                    await asyncio.sleep(RETRY_DELAY_SECONDS)
                else:
                    print(f"[Retry] All {max_attempts} attempts exhausted for {phone}.")
                    await update_schedule_status(schedule_id, "failed", attempt)
                    return
    except Exception as e:
        print(f"[Schedule Error] {e}")
        await update_schedule_status(schedule_id, "failed")

@app.post("/api/calls/schedule")
async def schedule_call(payload: ScheduleCallPayload, request: Request):
    """
    Endpoint for Mobile App: Schedule a call and define retry logic & multilingual settings.
    Spawns an async background task to trigger the call.
    """
    pool = getattr(request.app.state, "pool", None)
    
    import uuid
    schedule_id = str(uuid.uuid4())
    
    user_id = get_user_id(request)
    if pool:
        try:
            async with pool.acquire() as conn:
                await conn.execute('''
                    INSERT INTO scheduled_calls (id, phone_number, scheduled_time, language, retry_count, status, current_attempt, user_id)
                    VALUES ($1, $2, $3, $4, $5, $6, 0, $7)
                ''', schedule_id, payload.phone_number, parser.parse(payload.scheduled_time), payload.language or "en", payload.retry_count or 3, "pending", user_id)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Database error: {e}")
    else:
        raise HTTPException(status_code=500, detail="Database not connected")
    
    # Spawn background task immediately
    asyncio.create_task(_schedule_call_task(
        schedule_id=schedule_id,
        phone=payload.phone_number,
        scheduled_time=payload.scheduled_time,
        language=payload.language or "en",
        max_attempts=payload.retry_count or 3,
        user_id=user_id
    ))
    
    return {
        "status": "success",
        "id": schedule_id,
        "message": f"Call to {payload.phone_number} securely scheduled for {payload.scheduled_time}",
        "language_context": payload.language,
        "max_retries_configured": payload.retry_count
    }

@app.get("/api/calls/scheduled")
async def get_scheduled_calls(request: Request):
    """Returns all pending/completed scheduled calls for the dashboard UI from Neon DB."""
    pool = getattr(request.app.state, "pool", None)
    if not pool:
        return []
    user_id = get_user_id(request)
    try:
        async with pool.acquire() as conn:
            records = await conn.fetch("SELECT * FROM scheduled_calls WHERE user_id = $1 ORDER BY created_at DESC", user_id)
            
        result = []
        for r in records:
            d = dict(r)
            if d.get('scheduled_time'):
                d['scheduled_time'] = d['scheduled_time'].isoformat()
            if d.get('created_at'):
                d['created_at'] = d['created_at'].isoformat()
            
            # Reformat to match what UI expects: stringify status
            if "retry" in str(d.get("status")):
                d["status"] = f"retrying ({d.get('current_attempt', 0)}/{d.get('retry_count', 3)})"
                
            result.append(d)
        return result
    except Exception as e:
        print(f"Error fetching schedules: {e}")
        return []

@app.delete("/api/calls/scheduled/{schedule_id}")
async def cancel_scheduled_call(schedule_id: str, request: Request):
    """Marks a scheduled call as cancelled in the Neon DB store."""
    requested_id = schedule_id.strip()
    print(f"DEBUG: Cancel requested for ID: {requested_id}")
    pool = getattr(request.app.state, "pool", None)
    user_id = get_user_id(request)
    if pool:
        async with pool.acquire() as conn:
            # Check if exists and belongs to user
            exists = await conn.fetchval("SELECT id FROM scheduled_calls WHERE id = $1 AND user_id = $2", requested_id, user_id)
            if not exists:
                raise HTTPException(status_code=404, detail="Schedule not found or unauthorized")
                
            await conn.execute("UPDATE scheduled_calls SET status = 'cancelled' WHERE id = $1", requested_id)
            return {"status": "success", "message": "Schedule cancelled"}
    raise HTTPException(status_code=500, detail="Database not connected")

@app.get("/api/calls/history")
async def get_call_history(request: Request):
    """
    Returns the most recent 50 outgoing calls for the UI history list.
    """
    pool = getattr(request.app.state, "pool", None)
    if not pool:
        return []
        
    user_id = get_user_id(request)
    async with pool.acquire() as conn:
        records = await conn.fetch('''
            SELECT call_id, phone_number, status, intent, transcript, summary, created_at, recording_url 
            FROM call_transcripts 
            WHERE user_id = $1
            ORDER BY created_at DESC 
            LIMIT 50
        ''', user_id)
        
    # Serialize datetimes safely
    result = []
    for r in records:
        d = dict(r)
        if d.get('created_at'):
            d['created_at'] = d['created_at'].isoformat()
        result.append(d)
        
    return result

@app.get("/api/analytics")
async def get_dashboard_analytics(request: Request):
    """
    Returns aggregated metrics from the call_transcripts table, enriched by Groq AI analysis.
    """
    pool = getattr(request.app.state, "pool", None)
    if not pool:
        return {"total_runs": 0, "dispositions": [], "duration_stats": []}
        
    user_id = get_user_id(request)
    async with pool.acquire() as conn:
        # 1. Total calls
        total_runs = await conn.fetchval('SELECT COUNT(*) FROM call_transcripts WHERE user_id = $1', user_id)
        
        # 2. Intent breakdown (Groq-annotated)
        intent_records = await conn.fetch(
            "SELECT COALESCE(intent, 'UNKNOWN') as name, COUNT(*) as value FROM call_transcripts WHERE user_id = $1 GROUP BY intent", user_id
        )
        intent_breakdown = [dict(r) for r in intent_records]
        
        # 3. Conversion Rate: INTERESTED / total
        interested_count = await conn.fetchval(
            "SELECT COUNT(*) FROM call_transcripts WHERE UPPER(intent) = 'INTERESTED' AND user_id = $1", user_id
        )
        conversion_rate = round((int(interested_count or 0) / int(total_runs or 1)) * 100, 1)
        
        # 4. Callback rate
        callback_count = await conn.fetchval(
            "SELECT COUNT(*) FROM call_transcripts WHERE (UPPER(intent) = 'CALLBACK' OR callback_requested = true) AND user_id = $1", user_id
        )
        
        # 5. Lead Quality Breakdown (HOT/WARM/COLD)
        lead_quality_records = await conn.fetch(
            "SELECT COALESCE(lead_status, 'UNKNOWN') as name, COUNT(*) as value FROM call_transcripts WHERE user_id = $1 GROUP BY lead_status", user_id
        )
        lead_quality = [dict(r) for r in lead_quality_records]
        
        # 6. Sentiment Distribution
        sentiment_records = await conn.fetch(
            "SELECT COALESCE(sentiment, 'unknown') as name, COUNT(*) as value FROM call_transcripts WHERE user_id = $1 GROUP BY sentiment", user_id
        )
        sentiment_dist = [dict(r) for r in sentiment_records]
        
        # 7. Average engagement score
        avg_engagement = await conn.fetchval(
            "SELECT ROUND(AVG(engagement_score), 1) FROM call_transcripts WHERE engagement_score IS NOT NULL AND user_id = $1", user_id
        )
        
        # 8. Dispositions (call status)
        disp_records = await conn.fetch(
            "SELECT COALESCE(status, 'UNKNOWN') as name, COUNT(*) as value FROM call_transcripts WHERE user_id = $1 GROUP BY status", user_id
        )
        dispositions = [dict(r) for r in disp_records]
        
        # 9. Duration histogram
        duration_buckets = await conn.fetch('''
            SELECT 
                CASE 
                    WHEN call_duration_seconds <= 10 THEN '0-10s'
                    WHEN call_duration_seconds > 10 AND call_duration_seconds <= 30 THEN '10-30s'
                    WHEN call_duration_seconds > 30 AND call_duration_seconds <= 60 THEN '30-60s'
                    WHEN call_duration_seconds > 60 AND call_duration_seconds <= 120 THEN '60-120s'
                    WHEN call_duration_seconds > 120 AND call_duration_seconds <= 180 THEN '120-180s'
                    ELSE '>180s'
                END AS range,
                COUNT(*) as count
            FROM call_transcripts
            WHERE user_id = $1
            GROUP BY 
                CASE 
                    WHEN call_duration_seconds <= 10 THEN '0-10s'
                    WHEN call_duration_seconds > 10 AND call_duration_seconds <= 30 THEN '10-30s'
                    WHEN call_duration_seconds > 30 AND call_duration_seconds <= 60 THEN '30-60s'
                    WHEN call_duration_seconds > 60 AND call_duration_seconds <= 120 THEN '60-120s'
                    WHEN call_duration_seconds > 120 AND call_duration_seconds <= 180 THEN '120-180s'
                    ELSE '>180s'
                END
        ''', user_id)
        order = {'0-10s': 1, '10-30s': 2, '30-60s': 3, '60-120s': 4, '120-180s': 5, '>180s': 6}
        d_stats = sorted([dict(r) for r in duration_buckets], key=lambda x: order.get(x['range'], 99))

        return {
            "total_runs": int(total_runs or 0),
            "conversion_rate": float(conversion_rate),
            "interested_count": int(interested_count or 0),
            "callback_count": int(callback_count or 0),
            "avg_engagement": float(avg_engagement or 0),
            "intent_breakdown": intent_breakdown,
            "lead_quality": lead_quality,
            "sentiment_dist": sentiment_dist,
            "dispositions": dispositions,
            "duration_stats": d_stats
        }

@app.post("/api/analytics/backfill")
async def backfill_groq_analysis(request: Request):
    """
    Re-analyzes ALL existing transcripts with Groq AI and backfills
    missing lead_status, sentiment, intent, engagement_score, etc.
    """
    pool = getattr(request.app.state, "pool", None)
    if not pool:
        raise HTTPException(status_code=500, detail="Database not configured")

    async with pool.acquire() as conn:
        # Fetch records that are missing Groq analysis OR have generic values
        records = await conn.fetch('''
            SELECT id, call_id, transcript FROM call_transcripts
            WHERE transcript IS NOT NULL
              AND transcript != 'No transcript generated.'
              AND transcript != 'Call initiating...'
              AND (
                  lead_status IS NULL
                  OR sentiment IS NULL OR sentiment = 'unknown'
                  OR intent IS NULL OR intent IN ('unknown', 'pending', 'UNKNOWN')
              )
        ''')

    if not records:
        return {"status": "done", "updated": 0, "message": "All records already have Groq analysis."}

    updated = 0
    errors = 0
    for record in records:
        transcript = record["transcript"]
        call_id = record["call_id"]

        groq_result = await analyze_transcript_with_groq(transcript)
        if not groq_result:
            errors += 1
            continue

        try:
            async with pool.acquire() as conn:
                await conn.execute('''
                    UPDATE call_transcripts SET
                        intent = COALESCE($1, intent),
                        lead_status = COALESCE($2, lead_status),
                        sentiment = COALESCE($3, sentiment),
                        urgency_level = COALESCE($4, urgency_level),
                        product_interest = COALESCE($5, product_interest),
                        pain_point = COALESCE($6, pain_point),
                        next_step = COALESCE($7, next_step),
                        callback_requested = COALESCE($8, callback_requested),
                        engagement_score = COALESCE($9, engagement_score),
                        summary = COALESCE($10, summary)
                    WHERE call_id = $11
                ''',
                groq_result.get("intent"),
                groq_result.get("lead_status"),
                groq_result.get("sentiment"),
                groq_result.get("urgency_level"),
                groq_result.get("product_interest"),
                groq_result.get("pain_point"),
                groq_result.get("next_step"),
                groq_result.get("callback_requested"),
                groq_result.get("engagement_score"),
                groq_result.get("summary"),
                call_id
                )
                updated += 1
                print(f"Backfilled call {call_id}: {groq_result.get('intent')} / {groq_result.get('lead_status')} / {groq_result.get('sentiment')}")
        except Exception as e:
            print(f"Backfill DB error for {call_id}: {e}")
            errors += 1

    return {
        "status": "done",
        "total_candidates": len(records),
        "updated": updated,
        "errors": errors
    }

from fastapi.responses import StreamingResponse
import io
import csv

@app.get("/api/analytics/csv")
async def download_csv(request: Request):
    """
    Downloads all database transcripts securely as a CSV file.
    """
    pool = getattr(request.app.state, "pool", None)
    if not pool:
        raise HTTPException(status_code=500, detail="Database not configured")
        
    user_id = get_user_id(request)
    async with pool.acquire() as conn:
        records = await conn.fetch('SELECT * FROM call_transcripts WHERE user_id = $1', user_id)
        
    if not records:
         raise HTTPException(status_code=404, detail="No data available")
         
    # Generate CSV in memory
    stream = io.StringIO()
    writer = csv.writer(stream)
    
    # Extract keys from first record
    headers = list(records[0].keys())
    writer.writerow(headers)
    
    for row in records:
        writer.writerow([row[h] for h in headers])
        
    response = StreamingResponse(iter([stream.getvalue()]), media_type="text/csv")
    response.headers["Content-Disposition"] = "attachment; filename=transcripts.csv"
    return response

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "Vedaspark AI Orchestrator"}

class TriggerCallPayload(BaseModel):
    phone_number: str
    lead_name: str | None = "Unknown"
    lead_id: str | None = "undefined"
    language: str | None = "en"  # ISO 639-1 language code
    user_id: str | None = None

@app.post("/api/calls/trigger")
async def trigger_call(payload: TriggerCallPayload, request: Request):
    """
    Endpoint for Dashboard: Hits the Dograh AI API directly to trigger a call.
    """
    use_webhook = os.environ.get("USE_WEBHOOK", "false").lower() == "true"
    
    try:
        async with httpx.AsyncClient() as client:
            if use_webhook:
                webhook_url = os.environ.get("TRIGGER_WEBHOOK_URL", "https://horn-falls-consultant-nuclear.trycloudflare.com/webhook/espocrm")
                response = await client.post(
                    webhook_url,
                    headers={
                        "Content-Type": "application/json"
                    },
                    json={
                        "phone_number": payload.phone_number,
                        "lead_name": payload.lead_name or "Unknown",
                        "lead_id": payload.lead_id or "undefined"
                    }
                )
            else:
                api_key = os.environ.get("NEXT_PUBLIC_DOGRAH_API_KEY") or os.environ.get("DOGRAH_API_KEY")
                agent_id = os.environ.get("DOGRAH_AGENT_ID") or os.environ.get("AGENT_UUID") or "af96de66-753e-4201-b166-ce5eccab3951"
                
                if not api_key:
                    raise HTTPException(status_code=500, detail="Dograh credentials not configured in .env (Check NEXT_PUBLIC_DOGRAH_API_KEY or DOGRAH_API_KEY)")
                    
                dograh_url = f"https://api.dograh.com/api/v1/public/agent/{agent_id}"
                response = await client.post(
                    dograh_url,
                    headers={
                        "X-API-Key": api_key,
                        "Content-Type": "application/json"
                    },
                    json={
                        "phone_number": payload.phone_number,
                        "initial_context": {
                            "language": payload.language or "en",
                            "lead_name": payload.lead_name or "Unknown"
                        }
                    }
                )
            
            response.raise_for_status()
            
            try:
                data = response.json()
            except:
                data = {"message": response.text}
                
            print(f"API Response: {data}")
            
            # Extract or generate call ID
            import uuid
            call_id = str(data.get("workflow_run_id") or data.get("call_id") or data.get("id") or uuid.uuid4())
            
            # Prioritize payload user_id for mobile integration, then header
            user_id = payload.user_id or get_user_id(request)
            pool = getattr(request.app.state, "pool", None)
            if pool:
                try:
                    async with pool.acquire() as conn:
                        await conn.execute('''
                            INSERT INTO call_transcripts (call_id, phone_number, status, intent, transcript, language_spoken, caller_name, user_id)
                            VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                            ON CONFLICT (call_id) DO NOTHING
                        ''', call_id, payload.phone_number, "queued", "pending", "Call initiating...", payload.language or "en", payload.lead_name, user_id)
                except Exception as db_err:
                    print(f"Warning: Could not save initial trigger to Neon: {db_err}")
            
        return {"status": "success", "message": "Call dispatched successfully!", "call_data": data}
    except httpx.HTTPStatusError as e:
        raise HTTPException(status_code=e.response.status_code, detail=f"Trigger Error: {e.response.text}")
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

class LeadPayload(BaseModel):
    firstName: str
    lastName: str
    phoneNumber: str

class CreateLeadsPayload(BaseModel):
    leads: list[LeadPayload]

@app.post("/api/leads/create")
async def create_crm_leads(payload: CreateLeadsPayload):
    """
    Endpoint for Dashboard: Creates new leads directly in EspoCRM via webhook integration.
    """
    successful = 0
    errors = []
    
    for lead in payload.leads:
        data = {
            "firstName": lead.firstName,
            "lastName": lead.lastName,
            "phoneNumber": lead.phoneNumber,
            "status": "New"
        }
        
        try:
            res = await push_unified_event(
                lead={}, 
                event_type="LEAD_CREATE",
                data=data
            )
            if res:
                successful += 1
            else:
                errors.append(f"Failed to push {lead.firstName} {lead.lastName}")
        except Exception as e:
            errors.append(str(e))
            
    return {
        "status": "success",
        "total": len(payload.leads),
        "successful": successful,
        "errors": errors
    }


@app.get("/api/leads")
async def list_crm_contacts() -> list[dict]:
    """Fetches contact/lead details from EspoCRM for display in the dashboard.

    Returns a simplified list with name, phone, and status fields.
    """
    raw_leads = await fetch_crm_leads(limit=100)
    contacts: list[dict] = []

    for lead in raw_leads:
        first = (lead.get("firstName") or lead.get("first_name") or "").strip()
        last = (lead.get("lastName") or lead.get("last_name") or "").strip()
        name = (first + " " + last).strip() or lead.get("name") or "Unknown"
        phone = lead.get("phoneNumber") or lead.get("phone") or lead.get("mobilePhone") or lead.get("mobile")
        status = lead.get("status") or lead.get("leadStatus") or "Unknown"

        created_at = lead.get("createdAt") or lead.get("dateEntered") or lead.get("createdAtDate")
        updated_at = lead.get("updatedAt") or lead.get("dateModified")

        contacts.append({
            "id": lead.get("id"),
            "name": name,
            "phoneNumber": phone,
            "status": status,
            "createdAt": created_at,
            "updatedAt": updated_at,
        })

    return contacts


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
