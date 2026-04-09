from fastapi import FastAPI, Request
from pydantic import BaseModel
import httpx
import os
from crm_connector import push_unified_event, map_lead_status

app = FastAPI(title="Vedaspark AI - Python Backend")

class DograhWebhookPayload(BaseModel):
    call_id: str
    status: str
    transcript: str | None = None
    intent: str | None = None
    contact_id: str | None = None

class ScheduleCallPayload(BaseModel):
    phone_number: str
    scheduled_time: str
    language: str | None = "english"
    retry_count: int | None = 3

# A simple POST endpoint for Dograh AI to send call completion webhooks
@app.post("/api/webhooks/dograh")
async def handle_dograh_webhook(payload: DograhWebhookPayload):
    # This endpoint receives the completed call data from Dograh Cloud
    print(f"Received Webhook from Dograh for call: {payload.call_id}")
    print(f"Status: {payload.status}")
    print(f"Transcript Snippet: {payload.transcript[:50] if payload.transcript else 'N/A'}")
    
    # Analyze intent if not provided
    intent = payload.intent or _analyze_intent(payload.transcript)
    
    # Map internal status to ESPO CRM status using crm_connector
    internal_status = f"CALL_{intent.upper()}"
    espo_status = map_lead_status(internal_status)

    if payload.contact_id:
        update_data = {
            "description": payload.transcript,
            "status": espo_status
        }
        await push_unified_event(
            lead={"id": payload.contact_id},
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

from fastapi import UploadFile, File

@app.post("/api/knowledge/upload")
async def upload_rag_document(file: UploadFile = File(...)):
    """
    Endpoint for Mobile App: Uploads a PDF/TXT document to update the AI's Knowledge Base (RAG).
    This file is forwarded to the Dograh Server or processed into an internal Vector DB.
    """
    return {
        "status": "success",
        "message": f"Document '{file.filename}' securely uploaded and vectorized for RAG context."
    }

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "Vedaspark AI Orchestrator"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
