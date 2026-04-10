import asyncio
import sys
import os
import json

# Set up paths for Dograh internal imports
sys.path.append("/app")

from api.db.db_client import DBClient
from api.enums import OrganizationConfigurationKey as ConfigKey

# Twilio Credentials (loaded from environment)
TWILIO_ACCOUNT_SID = os.environ.get("TWILIO_ACCOUNT_SID", "YOUR_TWILIO_SID")
TWILIO_AUTH_TOKEN = os.environ.get("TWILIO_AUTH_TOKEN", "YOUR_TWILIO_AUTH_TOKEN")
TWILIO_PHONE_NUMBER = os.environ.get("TWILIO_PHONE_NUMBER", "+1XXXXXXXXXX")

async def initialize():
    db = DBClient()
    org_id = 3 # The user's org where the agent lives
    user_id = 1 # Admin user
    
    print(f"Initializing Workflow and Webhooks for Org {org_id}...")
    
    # 1. Ensure Telephony Config
    twilio_config = {
        "provider": "twilio",
        "account_sid": TWILIO_ACCOUNT_SID,
        "auth_token": TWILIO_AUTH_TOKEN,
        "from_numbers": [TWILIO_PHONE_NUMBER],
        "default_from_number": TWILIO_PHONE_NUMBER,
        "account_id": TWILIO_ACCOUNT_SID
    }
    await db.upsert_configuration(org_id, ConfigKey.TELEPHONY_CONFIGURATION.value, twilio_config)
    
    # 2. Define Workflow with Webhook Node
    workflow_definition = {
        "nodes": [
            {
                "id": "trigger-1",
                "type": "trigger",
                "position": {"x": 100, "y": 100},
                "data": {"name": "CRM Trigger"}
            },
            {
                "id": "agent-1",
                "type": "ai_agent",
                "position": {"x": 400, "y": 100},
                "data": {
                    "name": "Divya",
                    "prompt": (
                        "You are an AI assistant for Vedasparks. You are calling a new lead. "
                        "Greet them by name (if provided) and explain our services. "
                        "CRITICAL: If the user says they are interested or want to proceed, "
                        "you MUST explicitly say 'Great, I will mark you as interested.' "
                        "If they are polite but don't want to proceed, just thank them and end the call. "
                        "The system will analyze your summary to determine the CRM status."
                    )
                }
            },
            {
                "id": "webhook-1",
                "type": "webhook",
                "position": {"x": 700, "y": 100},
                "data": {
                    "name": "Sync Result Webhook",
                    "endpoint_url": "http://bridge:5000/callback/dograh",
                    "http_method": "POST",
                    "payload_template": {
                        "lead_id": "{{ initial_context.lead_id }}",
                        "workflow_run_id": "{{ workflow_run_id }}",
                        "status": "completed",
                        "gathered_context": "{{ gathered_context }}"
                    }
                }
            }
        ],
        "edges": [
            {"id": "e1", "source": "trigger-1", "target": "agent-1"},
            {"id": "e2", "source": "agent-1", "target": "webhook-1"}
        ]
    }
    
    # 3. Create or Refresh Workflow
    workflows = await db.get_all_workflows(organization_id=org_id)
    # We'll create a new one to be sure it has the webhook
    workflow = await db.create_workflow("Advanced Lead Outreach", workflow_definition, user_id, org_id)
    print(f"Created Advanced Workflow: ID {workflow.id}")
    
    # 4. Generate/Verify API Key
    _, raw_key = await db.create_api_key(org_id, "Advanced Bridge Key", user_id)
    
    # 5. Output for human
    print("\nINITIALIZATION COMPLETE")
    print(f"API_KEY={raw_key}")
    print(f"WORKFLOW_ID={workflow.id}")

if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.run_until_complete(initialize())
