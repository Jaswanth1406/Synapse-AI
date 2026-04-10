import os
import httpx
from fastapi import FastAPI, Request, HTTPException, BackgroundTasks
from pydantic import BaseModel
import asyncio
from loguru import logger
import json
from datetime import datetime

app = FastAPI()

# Configuration (Exclusively from environment variables/.env)
DOGRAH_API_URL = os.getenv("DOGRAH_API_URL")
DOGRAH_API_KEY = os.getenv("DOGRAH_API_KEY")
AGENT_UUID = os.getenv("AGENT_UUID")
ESPO_URL = os.getenv("ESPO_URL")
ESPO_API_KEY = os.getenv("ESPO_API_KEY")
TRANSCRIPT_DIR = os.getenv("TRANSCRIPT_DIR", "/app/transcripts")
RETRY_STORE = os.getenv("RETRY_STORE", "/app/retries.json")

os.makedirs(TRANSCRIPT_DIR, exist_ok=True)

# ──────────────────────────────────────────────────────────────────────
# RETRY & CALL GUARD CONFIGURATION
# ──────────────────────────────────────────────────────────────────────
RETRY_DELAY_SECONDS = 60          # 1 minute between retries
MIN_USER_REPLIES = 4              # Minimum user replies to count as a real conversation
MIN_CALL_DURATION_SECONDS = 60    # Minimum call duration (seconds) to count as meaningful
MAX_RETRY_ATTEMPTS = 10           # Safety cap to prevent infinite retries

# In-memory lock: tracks phone numbers that currently have an active call
# Format: { "+918939278994": True }
_active_calls: dict[str, bool] = {}
_active_calls_lock = asyncio.Lock()


class LeadWebhook(BaseModel):
    phone_number: str
    lead_name: str
    lead_id: str


# ──────────────────────────────────────────────────────────────────────
# CONCURRENT CALL GUARD
# Prevents calling the same number while a call is already in progress
# ──────────────────────────────────────────────────────────────────────
async def acquire_call_lock(phone: str) -> bool:
    """Try to lock a phone number. Returns True if acquired, False if already active."""
    async with _active_calls_lock:
        if _active_calls.get(phone):
            logger.warning(f"CALL GUARD: {phone} already has an active call. Skipping.")
            return False
        _active_calls[phone] = True
        logger.info(f"CALL GUARD: Locked {phone}")
        return True


async def release_call_lock(phone: str):
    """Release the lock on a phone number after call completes."""
    async with _active_calls_lock:
        _active_calls.pop(phone, None)
        logger.info(f"CALL GUARD: Released {phone}")


# ──────────────────────────────────────────────────────────────────────
# RETRY PERSISTENCE
# ──────────────────────────────────────────────────────────────────────
def get_retries():
    if os.path.exists(RETRY_STORE):
        with open(RETRY_STORE, "r") as f:
            return json.load(f)
    return {}


def save_retries(data):
    with open(RETRY_STORE, "w") as f:
        json.dump(data, f)


def increment_retry(lead_id: str) -> int:
    """Increment and return the new retry count for a lead."""
    retries = get_retries()
    count = retries.get(lead_id, 0) + 1
    retries[lead_id] = count
    save_retries(retries)
    return count


def clear_retry(lead_id: str):
    """Clear retry counter for a lead (conversation completed)."""
    retries = get_retries()
    retries.pop(lead_id, None)
    save_retries(retries)


# ──────────────────────────────────────────────────────────────────────
# CRM HELPERS
# ──────────────────────────────────────────────────────────────────────
async def create_crm_note(lead_id: str, text: str):
    logger.info(f"Creating CRM Note for Lead {lead_id}")
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.post(
                f"{ESPO_URL}/api/v1/Note",
                json={
                    "post": text,
                    "parentType": "Lead",
                    "parentId": lead_id,
                    "type": "Post"
                },
                headers={"X-Api-Key": ESPO_API_KEY}
            )
            if resp.status_code not in (200, 201):
                logger.error(f"Failed to create CRM Note: {resp.text}")
    except Exception as e:
        logger.error(f"CRM Note Error: {e}")


async def fetch_call_details(call_id: str):
    logger.info(f"Fetching deep call profile for ID: {call_id} from Dograh Cloud...")
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.get(
                f"{DOGRAH_API_URL}/api/v1/telephony/call/{call_id}",
                headers={"X-API-Key": DOGRAH_API_KEY}
            )
            if resp.status_code == 200:
                data = resp.json()
                # Return the phone number from the actual telephony session
                return data.get("to_number") or data.get("phone")
    except Exception as e:
        logger.error(f"Cloud Call Profile Error: {e}")
    return None


async def find_lead_by_phone(phone: str):
    logger.info(f"Searching CRM for Lead with phone: {phone}")
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.get(
                f"{ESPO_URL}/api/v1/Lead",
                params={
                    "where[0][type]": "equals",
                    "where[0][attribute]": "phoneNumber",
                    "where[0][value]": phone
                },
                headers={"X-Api-Key": ESPO_API_KEY}
            )
            data = resp.json()
            if data.get("list"):
                return data["list"][0]["id"]
    except Exception as e:
        logger.error(f"Lead Lookup Error: {e}")
    return None


async def update_crm_status(lead_id: str, status: str):
    logger.info(f"Updating CRM Lead {lead_id} to status: {status}")
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.patch(
                f"{ESPO_URL}/api/v1/Lead/{lead_id}",
                json={"status": status},
                headers={"X-Api-Key": ESPO_API_KEY}
            )
            if resp.status_code != 200:
                logger.error(f"Failed to update CRM: {resp.text}")
    except Exception as e:
        logger.error(f"CRM Update Error: {e}")


# ──────────────────────────────────────────────────────────────────────
# DOGRAH CALL TRIGGER (with call guard)
# ──────────────────────────────────────────────────────────────────────
async def initiate_dograh_call(lead_id: str, phone_number: str, lead_name: str):
    """Initiate a call ONLY if no active call exists for this number."""

    # Guard: prevent duplicate concurrent calls to the same number
    if not await acquire_call_lock(phone_number):
        logger.warning(f"BLOCKED: Call to {phone_number} rejected — already in progress.")
        return None

    logger.info(f"Initiating Dograh call for {lead_name} ({phone_number})")
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.post(
                f"{DOGRAH_API_URL}/api/v1/public/agent/{AGENT_UUID}",
                json={
                    "phone_number": phone_number,
                    "to": phone_number,
                    "phone": phone_number,
                    "initial_context": {
                        "lead_name": lead_name,
                        "lead_id": lead_id,
                        "phone": phone_number
                    }
                },
                headers={"X-API-Key": DOGRAH_API_KEY}
            )
            if resp.status_code != 200:
                logger.error(f"Dograh trigger failed: {resp.text}")
                await release_call_lock(phone_number)
                return None
            return resp.json()
    except Exception as e:
        logger.error(f"Dograh Trigger Error: {e}")
        await release_call_lock(phone_number)
        return None


# ──────────────────────────────────────────────────────────────────────
# CONVERSATION QUALITY ANALYSIS
# Determines if the call was a real conversation or just a missed/short call
# ──────────────────────────────────────────────────────────────────────
def analyze_conversation(data: dict) -> dict:
    """
    Analyze callback data to determine conversation quality.

    Returns:
        {
            "user_reply_count": int,     # How many times the user spoke
            "call_duration": float,      # Duration in seconds
            "was_answered": bool,        # Whether the call was picked up
            "is_meaningful": bool,       # True if 4-5+ replies OR 60s+ duration
            "reason": str               # Human-readable explanation
        }
    """
    gathered_context = data.get("gathered_context", {})
    transcript = data.get("transcript", [])
    call_status = str(data.get("status", "")).lower()
    call_duration = float(data.get("duration", 0) or data.get("call_duration", 0) or 0)

    # Count user replies from transcript array
    user_reply_count = 0
    if isinstance(transcript, list):
        for entry in transcript:
            role = str(entry.get("role", "") or entry.get("speaker", "")).lower()
            if role in ("user", "human", "customer", "caller"):
                user_reply_count += 1

    # Fallback: count from gathered_context if transcript is empty
    if user_reply_count == 0 and gathered_context:
        # Check for conversation turns or messages array
        messages = gathered_context.get("messages", [])
        if isinstance(messages, list):
            for msg in messages:
                if str(msg.get("role", "")).lower() in ("user", "human"):
                    user_reply_count += 1

        # Try extracting reply count from summary text
        summary = str(gathered_context.get("summary", ""))
        context_str = str(gathered_context)

        # If summary mentions very short interaction
        if any(kw in context_str.lower() for kw in ["no answer", "didn't pick", "voicemail",
                                                      "not reachable", "busy", "rejected",
                                                      "hung up immediately", "disconnected"]):
            user_reply_count = 0

    # Determine if call was answered
    was_answered = call_status not in ("no-answer", "busy", "failed", "canceled", "rejected", "no_answer")
    if user_reply_count == 0 and call_duration < 5:
        was_answered = False

    # Determine if conversation was meaningful
    is_meaningful = (user_reply_count >= MIN_USER_REPLIES) or (call_duration >= MIN_CALL_DURATION_SECONDS)

    # Build reason
    if not was_answered:
        reason = f"Call not answered (status: {call_status}, duration: {call_duration}s)"
    elif not is_meaningful:
        reason = f"Short conversation ({user_reply_count} replies, {call_duration}s) — needs {MIN_USER_REPLIES}+ replies or {MIN_CALL_DURATION_SECONDS}s+"
    else:
        reason = f"Meaningful conversation ({user_reply_count} replies, {call_duration}s)"

    return {
        "user_reply_count": user_reply_count,
        "call_duration": call_duration,
        "was_answered": was_answered,
        "is_meaningful": is_meaningful,
        "reason": reason
    }


def determine_outcome(data: dict, analysis: dict) -> str:
    """
    Determine the CRM outcome based on conversation quality.

    Outcomes:
        - "No Answer"   → Call was not picked up
        - "Incomplete"  → Picked up but < 4-5 replies (too short)
        - "Converted"   → Meaningful conversation + user showed interest
        - "Completed"   → Meaningful conversation finished normally
    """
    if not analysis["was_answered"]:
        return "No Answer"

    if not analysis["is_meaningful"]:
        return "Incomplete"

    # Only check for interest/completion if conversation was meaningful
    gathered_context = data.get("gathered_context", {})
    ai_summary = str(gathered_context).lower()

    if "interested" in ai_summary or "proceed" in ai_summary:
        return "Converted"

    return "Completed"


# ──────────────────────────────────────────────────────────────────────
# RETRY TASK
# Retries in 1 minute. Keeps retrying until meaningful conversation
# or MAX_RETRY_ATTEMPTS is hit.
# ──────────────────────────────────────────────────────────────────────
async def retry_task(lead_id: str, phone_number: str, lead_name: str):
    """Schedule a retry call after RETRY_DELAY_SECONDS (1 minute)."""
    attempt = increment_retry(lead_id)

    if attempt > MAX_RETRY_ATTEMPTS:
        logger.warning(f"MAX RETRIES ({MAX_RETRY_ATTEMPTS}) reached for Lead {lead_id} ({phone_number}). Giving up.")
        await update_crm_status(lead_id, "Dead")
        await create_crm_note(lead_id, f"**Auto-Retry Exhausted**\n\nReached maximum {MAX_RETRY_ATTEMPTS} retry attempts without a meaningful conversation. Lead marked as Dead.")
        clear_retry(lead_id)
        return

    logger.info(f"RETRY #{attempt}/{MAX_RETRY_ATTEMPTS} for Lead {lead_id} ({phone_number}) in {RETRY_DELAY_SECONDS}s...")
    await asyncio.sleep(RETRY_DELAY_SECONDS)

    result = await initiate_dograh_call(lead_id, phone_number, lead_name)
    if result:
        # Register the new call/run IDs for identity tracking
        rid = result.get("workflow_run_id") or result.get("run_id") or result.get("id")
        if rid:
            save_to_registry(rid, lead_id)
        cid = result.get("telephony", {}).get("call_id")
        if cid:
            save_to_registry(cid, lead_id)
        logger.info(f"Retry call triggered successfully for Lead {lead_id}")
    else:
        logger.error(f"Retry call FAILED for Lead {lead_id}. Will be retried on next callback or manually.")


# ──────────────────────────────────────────────────────────────────────
# REGISTRY (maps Call/Run IDs → Lead IDs)
# ──────────────────────────────────────────────────────────────────────
CALL_REGISTRY = "/app/call_registry.json"


def get_registry():
    if os.path.exists(CALL_REGISTRY):
        try:
            with open(CALL_REGISTRY, "r") as f:
                return json.load(f)
        except: return {}
    return {}


def save_to_registry(call_id, lead_id):
    reg = get_registry()
    reg[str(call_id)] = lead_id
    with open(CALL_REGISTRY, "w") as f:
        json.dump(reg, f)


# ──────────────────────────────────────────────────────────────────────
# PHONE → LEAD ID REVERSE LOOKUP (for call guard release)
# ──────────────────────────────────────────────────────────────────────
LEAD_PHONE_REGISTRY = "/app/lead_phones.json"


def save_lead_phone(lead_id: str, phone: str):
    """Store lead_id → phone mapping for reverse lookup."""
    try:
        data = {}
        if os.path.exists(LEAD_PHONE_REGISTRY):
            with open(LEAD_PHONE_REGISTRY, "r") as f:
                data = json.load(f)
        data[lead_id] = phone
        with open(LEAD_PHONE_REGISTRY, "w") as f:
            json.dump(data, f)
    except Exception as e:
        logger.error(f"Lead phone registry error: {e}")


def get_lead_phone(lead_id: str) -> str | None:
    """Get phone number for a lead_id."""
    try:
        if os.path.exists(LEAD_PHONE_REGISTRY):
            with open(LEAD_PHONE_REGISTRY, "r") as f:
                data = json.load(f)
                return data.get(lead_id)
    except:
        pass
    return None


# ──────────────────────────────────────────────────────────────────────
# WEBHOOK: CRM → Bridge (New Lead)
# ──────────────────────────────────────────────────────────────────────
@app.post("/webhook/espocrm")
async def handle_espocrm_webhook(lead: LeadWebhook):
    logger.info(f"New Lead Webhook: {lead.lead_name} ({lead.phone_number})")

    # Save phone mapping for later reverse lookup
    save_lead_phone(lead.lead_id, lead.phone_number)

    # Initialize call (call guard is inside initiate_dograh_call)
    result = await initiate_dograh_call(lead.lead_id, lead.phone_number, lead.lead_name)
    if result:
        logger.info(f"CLOUD TRIGGER RESPONSE: {json.dumps(result)}")

        # 1. Capture internal Run ID
        rid = result.get("workflow_run_id") or result.get("run_id") or result.get("id")
        if rid:
            logger.info(f"Identity Locked (Run): Mapping {rid} to Lead {lead.lead_id}")
            save_to_registry(rid, lead.lead_id)

        # 2. Capture Telephony Call ID (If available immediately)
        cid = result.get("telephony", {}).get("call_id")
        if cid:
            logger.info(f"Identity Locked (Call): Mapping {cid} to Lead {lead.lead_id}")
            save_to_registry(cid, lead.lead_id)

        return {"status": "triggered", "run_id": rid, "call_id": cid}
    raise HTTPException(status_code=500, detail="Failed to trigger call")


# ──────────────────────────────────────────────────────────────────────
# CALLBACK: Dograh → Bridge (Call Finished)
# ──────────────────────────────────────────────────────────────────────
@app.post("/callback/dograh")
async def handle_dograh_callback(request: Request, background_tasks: BackgroundTasks):
    data = await request.json()
    logger.debug(f"RAW CALLBACK JSON: {json.dumps(data)}")

    # ── 1. Identify the Lead ──
    reg = get_registry()
    call_id = data.get("call_id")
    run_id = data.get("workflow_run_id")

    lead_id = reg.get(str(call_id)) or reg.get(str(run_id)) or data.get("lead_id")
    phone = data.get("phone_number")

    if not lead_id and phone:
        logger.info("Identity missing. Falling back to phone lookup...")
        lead_id = await find_lead_by_phone(phone)

    if not lead_id:
        logger.warning(f"CRITICAL: Could not identify Lead for Call {call_id}/{run_id}. Data: {data}")
        return {"status": "error", "message": "Lead not identified"}

    logger.info(f"Identity RECOVERED: Call {call_id} belongs to Lead {lead_id}")

    # Resolve phone number (needed for call guard release & retries)
    if not phone:
        phone = get_lead_phone(lead_id)

    # ── 2. Release the call guard for this number ──
    if phone:
        await release_call_lock(phone)

    # ── 3. Analyze conversation quality ──
    analysis = analyze_conversation(data)
    logger.info(f"CALL ANALYSIS for Lead {lead_id}: {json.dumps(analysis)}")

    # ── 4. Determine outcome ──
    outcome = determine_outcome(data, analysis)
    logger.info(f"OUTCOME for Lead {lead_id}: {outcome} — {analysis['reason']}")

    gathered_context = data.get("gathered_context", {})
    transcript_url = data.get("transcript_url")

    # ── 5. Store transcript/context as JSON ──
    t_path = os.path.join(TRANSCRIPT_DIR, f"{lead_id}_{call_id or run_id}.json")
    with open(t_path, "w") as f:
        json.dump(data, f)

    # ── 6. Create CRM Note ──
    retries = get_retries()
    attempt_num = retries.get(lead_id, 0)

    note_text = f"**AI Call Result: {outcome}**\n\n"
    note_text += f"**Analysis**: {analysis['reason']}\n"
    note_text += f"**User Replies**: {analysis['user_reply_count']} | **Duration**: {analysis['call_duration']}s\n"
    if attempt_num > 0:
        note_text += f"**Retry Attempt**: #{attempt_num}\n"
    note_text += f"**Summary**: {gathered_context.get('summary', 'Call finished.')}\n"
    if transcript_url:
        note_text += f"\n[Full Transcript]({transcript_url})"

    background_tasks.add_task(create_crm_note, lead_id, note_text)

    # ── 7. Update CRM Status ──
    background_tasks.add_task(update_crm_status, lead_id, outcome)

    # ── 8. Handle Retry Logic ──
    # Retry if: No Answer OR Incomplete (short conversation)
    # Stop retrying if: Completed OR Converted (meaningful conversation happened)
    if outcome in ("No Answer", "Incomplete"):
        if phone:
            logger.info(f"Scheduling RETRY for Lead {lead_id} ({phone}) — reason: {outcome}")
            init_ctx = data.get("initial_context", {})
            name = init_ctx.get("lead_name", "Lead")
            background_tasks.add_task(retry_task, lead_id, phone, name)
        else:
            logger.error(f"Cannot retry Lead {lead_id} — phone number unknown!")
    else:
        # Conversation was meaningful — clear retry counter
        clear_retry(lead_id)
        logger.info(f"Lead {lead_id} marked as {outcome}. Retries cleared.")

    return {
        "status": "received",
        "detected_outcome": outcome,
        "analysis": analysis
    }


@app.post("/")
async def root_fallback(request: Request, background_tasks: BackgroundTasks):
    logger.warning("Received callback on root / instead of /callback/dograh. Redirecting...")
    return await handle_dograh_callback(request, background_tasks)


# ──────────────────────────────────────────────────────────────────────
# STATUS ENDPOINTS
# ──────────────────────────────────────────────────────────────────────
@app.get("/health")
async def health():
    return {"status": "ok"}


@app.get("/status/active-calls")
async def active_calls_status():
    """Debug endpoint: see which numbers have active calls."""
    async with _active_calls_lock:
        return {
            "active_calls": list(_active_calls.keys()),
            "count": len(_active_calls)
        }


@app.get("/status/retries")
async def retries_status():
    """Debug endpoint: see retry counts per lead."""
    return get_retries()
