from fastapi import FastAPI, Request, HTTPException
from fastapi.exceptions import RequestValidationError
from fastapi.responses import Response
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from pydantic import BaseModel
import httpx
import os
import asyncpg
from dotenv import load_dotenv
load_dotenv("../frontend/.env") 
from twilio.rest import Client
from crm_connector import push_unified_event, map_lead_status

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
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            ''')
        print("Neon Database ready and tables verified.")
    else:
        app.state.pool = None
        print("WARNING: DATABASE_URL not found, DB storage disabled.")
    
    yield
    
    # Shutdown gracefully
    if getattr(app.state, "pool", None):
        await app.state.pool.close()

# Forcing auto-reload to recreate Neon DB tables
app = FastAPI(title="Vedaspark AI - Python Backend", lifespan=lifespan)

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

# Removed DograhWebhookPayload to prevent strict 422 validation errors. We will parse it manually.

class ScheduleCallPayload(BaseModel):
    phone_number: str
    scheduled_time: str
    language: str | None = "english"
    retry_count: int | None = 3

# A simple POST endpoint for Dograh AI to send call completion webhooks
@app.post("/api/webhooks/dograh")
async def handle_dograh_webhook(request: Request):
    # Dynamically accept ANY JSON Dograh sends so we never crash with 422
    payload = await request.json()
    call_id = str(payload.get("call_id", "unknown_id"))
    status = payload.get("status", "completed")
    
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
            
    # Extract Custom Extracted Variables safely
    intent = payload.get("intent", "unknown")
    contact_id = payload.get("contact_id")
    
    pool = getattr(request.app.state, "pool", None)
    if pool:
        try:
            async with pool.acquire() as conn:
                await conn.execute('''
                    INSERT INTO call_transcripts (
                        call_id, contact_id, phone_number, status, intent, transcript,
                        caller_name, company_name, designation, email_provided,
                        callback_requested, callback_time, language_spoken, sentiment,
                        call_outcome, objection_raised, objection_detail, product_interest,
                        budget_mentioned, budget_range, urgency_level, decision_maker,
                        competitor_mentioned, pain_point, next_step, meeting_date,
                        call_duration_seconds, questions_asked, referral_source, do_not_call
                    )
                    VALUES (
                        $1, $2, $3, $4, $5, $6,
                        $7, $8, $9, $10,
                        $11, $12, $13, $14,
                        $15, $16, $17, $18,
                        $19, $20, $21, $22,
                        $23, $24, $25, $26,
                        $27, $28, $29, $30
                    )
                    ON CONFLICT (call_id) DO UPDATE SET
                        status = EXCLUDED.status,
                        intent = EXCLUDED.intent,
                        transcript = EXCLUDED.transcript,
                        caller_name = EXCLUDED.caller_name,
                        company_name = EXCLUDED.company_name,
                        designation = EXCLUDED.designation,
                        email_provided = EXCLUDED.email_provided,
                        callback_requested = EXCLUDED.callback_requested,
                        callback_time = EXCLUDED.callback_time,
                        language_spoken = EXCLUDED.language_spoken,
                        sentiment = EXCLUDED.sentiment,
                        call_outcome = EXCLUDED.call_outcome,
                        objection_raised = EXCLUDED.objection_raised,
                        objection_detail = EXCLUDED.objection_detail,
                        product_interest = EXCLUDED.product_interest,
                        budget_mentioned = EXCLUDED.budget_mentioned,
                        budget_range = EXCLUDED.budget_range,
                        urgency_level = EXCLUDED.urgency_level,
                        decision_maker = EXCLUDED.decision_maker,
                        competitor_mentioned = EXCLUDED.competitor_mentioned,
                        pain_point = EXCLUDED.pain_point,
                        next_step = EXCLUDED.next_step,
                        meeting_date = EXCLUDED.meeting_date,
                        call_duration_seconds = EXCLUDED.call_duration_seconds,
                        questions_asked = EXCLUDED.questions_asked,
                        referral_source = EXCLUDED.referral_source,
                        do_not_call = EXCLUDED.do_not_call
                ''',
                call_id, contact_id, payload.get("phone_number"), status, intent, transcript_text,
                payload.get("caller_name"), payload.get("company_name"), payload.get("designation"), payload.get("email_provided"),
                str(payload.get("callback_requested")).lower() == 'true', payload.get("callback_time"), payload.get("language_spoken"), payload.get("sentiment"),
                payload.get("call_outcome"), str(payload.get("objection_raised")).lower() == 'true', payload.get("objection_detail"), payload.get("product_interest"),
                str(payload.get("budget_mentioned")).lower() == 'true', payload.get("budget_range"), payload.get("urgency_level"), str(payload.get("decision_maker")).lower() == 'true',
                payload.get("competitor_mentioned"), payload.get("pain_point"), payload.get("next_step"), payload.get("meeting_date"),
                int(payload.get("duration", 0) or 0), payload.get("questions_asked"), payload.get("referral_source"), str(payload.get("do_not_call")).lower() == 'true'
                )
            print("Successfully stored transcript in Neon Database.")
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



# --- Advanced Feature Endpoints for Mobile App ---

@app.post("/api/calls/schedule")
async def schedule_call(payload: ScheduleCallPayload):
    """
    Endpoint for Mobile App: Schedule a call and define retry logic & multilingual settings.
    A background cron job will pick this up and hit the Dograh Trigger API at `scheduled_time`.
    """
    # Logic to save to Neon DB goes here
    return {
        "status": "success", 
        "message": f"Call to {payload.phone_number} scheduled for {payload.scheduled_time}",
        "language_context": payload.language,
        "max_retries_configured": payload.retry_count
    }

@app.get("/api/calls/history")
async def get_call_history(request: Request):
    """
    Returns the most recent 50 outgoing calls for the UI history list.
    """
    pool = getattr(request.app.state, "pool", None)
    if not pool:
        return []
        
    async with pool.acquire() as conn:
        records = await conn.fetch('''
            SELECT call_id, phone_number, status, intent, transcript, created_at 
            FROM call_transcripts 
            ORDER BY created_at DESC 
            LIMIT 50
        ''')
        
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
    Returns aggregated metrics from the call_transcripts table.
    """
    pool = getattr(request.app.state, "pool", None)
    if not pool:
        return {"total_runs": 0, "dispositions": [], "duration_stats": []}
        
    async with pool.acquire() as conn:
        # 1. Total Workflow Runs
        total_runs = await conn.fetchval('SELECT COUNT(*) FROM call_transcripts')
        
        # 2. Transferred Calls (Assuming status or intent corresponds to XFER, or just 0 for now)
        transfer_count = await conn.fetchval("SELECT COUNT(*) FROM call_transcripts WHERE status='transferred' OR intent='transfer'")
        
        # 3. Dispositions
        disp_records = await conn.fetch("SELECT COALESCE(status, 'UNKNOWN') as name, COUNT(*) as value FROM call_transcripts GROUP BY status")
        dispositions = [dict(r) for r in disp_records]
        
        # 4. Duration Stats
        # Build histogram buckets: 0-10s, 10-30s, 30-60s, 60-120s, 120-180s, >180s
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
            GROUP BY 
                CASE 
                    WHEN call_duration_seconds <= 10 THEN '0-10s'
                    WHEN call_duration_seconds > 10 AND call_duration_seconds <= 30 THEN '10-30s'
                    WHEN call_duration_seconds > 30 AND call_duration_seconds <= 60 THEN '30-60s'
                    WHEN call_duration_seconds > 60 AND call_duration_seconds <= 120 THEN '60-120s'
                    WHEN call_duration_seconds > 120 AND call_duration_seconds <= 180 THEN '120-180s'
                    ELSE '>180s'
                END
        ''')
        
        # Ensure ordered manually in Python to guarantee correct sequence on chart!
        order = {'0-10s': 1, '10-30s': 2, '30-60s': 3, '60-120s': 4, '120-180s': 5, '>180s': 6}
        d_stats = sorted([dict(r) for r in duration_buckets], key=lambda x: order.get(x['range'], 99))

        return {
            "total_runs": total_runs or 0,
            "transfer_count": transfer_count or 0,
            "dispositions": dispositions,
            "duration_stats": d_stats
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
        
    async with pool.acquire() as conn:
        records = await conn.fetch('SELECT * FROM call_transcripts')
        
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

@app.post("/api/calls/trigger")
async def trigger_call(payload: TriggerCallPayload, request: Request):
    """
    Endpoint for Dashboard: Hits the Dograh AI API directly to trigger a call.
    Since Dograh has Twilio configured in its own dashboard, it handles the dialing!
    """
    api_key = os.environ.get("NEXT_PUBLIC_DOGRAH_API_KEY")
    agent_id = os.environ.get("DOGRAH_AGENT_ID", "af96de66-753e-4201-b166-ce5eccab3951")
    
    if not api_key:
        raise HTTPException(status_code=500, detail="Dograh credentials not configured in .env")
        
    try:
        dograh_url = f"https://api.dograh.com/api/v1/public/agent/{agent_id}"
        
        async with httpx.AsyncClient() as client:
            response = await client.post(
                dograh_url,
                headers={
                    "X-API-Key": api_key,
                    "Content-Type": "application/json"
                },
                json={
                    "phone_number": payload.phone_number,
                    "initial_context": {}
                }
            )
            
            response.raise_for_status()
            data = response.json()
            print(f"Dograh API Response: {data}")
            
            # Extract the actual ID Dograh uses
            call_id = str(data.get("workflow_run_id") or data.get("call_id") or data.get("id") or "unknown_id")
            
            # Immediately log to Database as "queued"
            pool = getattr(request.app.state, "pool", None)
            if pool:
                try:
                    async with pool.acquire() as conn:
                        await conn.execute('''
                            INSERT INTO call_transcripts (call_id, status, intent, transcript)
                            VALUES ($1, $2, $3, $4)
                            ON CONFLICT (call_id) DO NOTHING
                        ''', call_id, "queued", "pending", "Call initiating...")
                except Exception as db_err:
                    print(f"Warning: Could not save initial trigger to Neon: {db_err}")
            
        return {"status": "success", "message": "Dograh Agent deployed successfully!", "call_data": data}
    except httpx.HTTPStatusError as e:
        raise HTTPException(status_code=e.response.status_code, detail=f"Dograh API Error: {e.response.text}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
