import os
import httpx
import json
import base64
from datetime import datetime
from typing import Dict, Any, List, Optional
from dotenv import load_dotenv

load_dotenv()

# Configuration from .env
CRM_BASE_URL = os.getenv("ESPO_CRM_API_URL", "https://f556-180-235-121-242.ngrok-free.app/api").rstrip('/')
CRM_USER = os.getenv("ESPO_CRM_USER", "admin")
CRM_PASS = os.getenv("ESPO_CRM_PASS", "password")

def get_auth_header() -> Dict[str, str]:
    """Generates the Basic Auth header for EspoCRM."""
    auth_str = f"{CRM_USER}:{CRM_PASS}"
    encoded_auth = base64.b64encode(auth_str.encode()).decode()
    return {"Authorization": f"Basic {encoded_auth}"}

def map_lead_status(status: str) -> str:
    """Maps internal system statuses to EspoCRM lead statuses."""
    if not status:
        return 'New'
    mappings = {
        'CALL_IDLE': 'New',
        'CALL_CONNECTED': 'In Process',
        'CALL_INTERESTED': 'In Process',
        'CALL_NOT_INTERESTED': 'Recycled',
        'CALL_COMPLETED': 'In Process',
        'SMS_IDLE': 'New',
        'SMS_SENT': 'In Process',
        'NEW_INBOUND': 'New'
    }
    return mappings.get(status, 'Assigned')

async def push_unified_event(lead: Dict[str, Any], event_type: str, data: Dict[str, Any]):
    """Generic function to push any event (Lead, Task, Opp, etc.) to the CRM."""
    if not CRM_BASE_URL:
        return

    entity_config = {
        'LEAD_UPDATE': {'endpoint': f'/v1/Lead/{lead.get("lead_id") or lead.get("id")}', 'method': 'PUT', 'entity': 'Lead'},
        'TASK_LOG': {'endpoint': '/v1/Task', 'method': 'POST', 'entity': 'Task'},
        'OPPORTUNITY': {'endpoint': '/v1/Opportunity', 'method': 'POST', 'entity': 'Opportunity'},
        'MEETING': {'endpoint': '/v1/Meeting', 'method': 'POST', 'entity': 'Meeting'},
        'NOTE': {'endpoint': '/v1/Note', 'method': 'POST', 'entity': 'Note'}
    }

    config = entity_config.get(event_type)
    if not config:
        return

    url = f"{CRM_BASE_URL}{config['endpoint']}"
    method = config['method']
    headers = get_auth_header()

    try:
        async with httpx.AsyncClient() as client:
            res = await client.request(
                method,
                url,
                json=data,
                headers={**headers, "Content-Type": "application/json"}
            )
            res.raise_for_status()
            return res.json()
    except Exception as e:
        print(f"❌ CRM Push Failed [{event_type}]: {str(e)}")

async def check_connection() -> bool:
    """Verifies if the CRM is reachable and credentials are valid."""
    try:
        async with httpx.AsyncClient() as client:
            await client.get(
                f"{CRM_BASE_URL}/v1/Metadata",
                headers=get_auth_header(),
                timeout=3.0
            )
            return True
    except Exception:
        return False
