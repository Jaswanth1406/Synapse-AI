from fastapi import FastAPI, Request
from pydantic import BaseModel
import httpx
import os

app = FastAPI(title="Vedaspark AI - Python Backend")

class DograhWebhookPayload(BaseModel):
    call_id: str
    status: str
    transcript: str | None = None
    intent: str | None = None
    contact_id: str | None = None

# A simple POST endpoint for Dograh AI to send call completion webhooks
@app.post("/api/webhooks/dograh")
async def handle_dograh_webhook(payload: DograhWebhookPayload):
    # This endpoint receives the completed call data from Dograh Cloud
    print(f"Received Webhook from Dograh for call: {payload.call_id}")
    print(f"Status: {payload.status}")
    print(f"Transcript Snippet: {payload.transcript[:50] if payload.transcript else 'N/A'}")
    
    # Analyze intent if not provided
    intent = payload.intent or _analyze_intent(payload.transcript)
    
    # CRM Integration logic (Push to ESPO CRM)
    # _push_to_espo_crm(payload.contact_id, payload.transcript, intent)
    
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

def _push_to_espo_crm(contact_id: str | None, transcript: str | None, intent: str):
    """
    Simulated Push to ESPO CRM
    """
    espo_url = os.getenv("ESPO_CRM_API_URL")
    espo_key = os.getenv("ESPO_CRM_API_KEY")
    if not espo_url or not espo_key:
        print("ESPO CRM credentials not configured. Skipping CRM sync.")
        return

    headers = {"X-Api-Key": espo_key, "Content-Type": "application/json"}
    data = {
        "description": transcript,
        "leadStatus": intent
    }
    
    # Uncomment to actually push when CRM is live
    # with httpx.Client() as client:
    #     response = client.put(f"{espo_url}Lead/{contact_id}", headers=headers, json=data)
    #     print(f"ESPO CRM Sync: {response.status_code}")

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "Vedaspark AI Orchestrator"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
