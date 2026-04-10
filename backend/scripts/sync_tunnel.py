import subprocess
import time
import re
import os
import requests
import json
from pathlib import Path

# Configuration
ENV_FILE = "c:/Users/D sanjay kumar/Downloads/Synapse/.env"
DOGRAH_API_BASE = "https://api.dograh.com/api/v1"
WORKFLOW_ID = 2393  # Your Advanced Outreach Workflow (Cloud)

def get_latest_tunnel_url():
    """Extract the current trycloudflare.com URL from docker logs."""
    try:
        result = subprocess.run(
            ["docker", "logs", "cloudflared"],
            capture_output=True,
            text=True,
            check=True
        )
        # Search for the link pattern
        matches = re.findall(r'https://[a-zA-Z0-9-]+\.trycloudflare\.com', result.stderr)
        if matches:
            return matches[-1] # Return the most recent one
    except Exception as e:
        print(f"Error reading logs: {e}")
    return None

def update_env(new_url):
    """Update the BRIDGE_PUBLIC_URL in the .env file."""
    if not os.path.exists(ENV_FILE):
        return False
    
    with open(ENV_FILE, 'r') as f:
        lines = f.readlines()
    
    updated = False
    new_lines = []
    for line in lines:
        if line.startswith("BRIDGE_PUBLIC_URL="):
            new_lines.append(f"BRIDGE_PUBLIC_URL={new_url}\n")
            updated = True
        else:
            new_lines.append(line)
            
    if not updated:
        new_lines.append(f"BRIDGE_PUBLIC_URL={new_url}\n")
        
    with open(ENV_FILE, 'w') as f:
        f.writelines(new_lines)
    return True

def get_dograh_creds():
    """Load API Key from .env."""
    creds = {"key": None}
    if os.path.exists(ENV_FILE):
        with open(ENV_FILE, 'r') as f:
            for line in f:
                if line.startswith("DOGRAH_API_KEY="):
                    creds["key"] = line.split("=")[1].strip()
    return creds

def update_dograh_workflow(new_url, api_key):
    """Automatically update the Webhook node in your Dograh Cloud workflow."""
    headers = {"X-API-KEY": api_key, "Content-Type": "application/json"}
    webhook_callback_url = f"{new_url}/callback/dograh"
    
    try:
        # 1. Fetch current workflow
        print(f"Fetching workflow {WORKFLOW_ID}...")
        resp = requests.get(f"{DOGRAH_API_BASE}/workflow/fetch/{WORKFLOW_ID}", headers=headers)
        if resp.status_code != 200:
            print(f"Failed to fetch: {resp.text}")
            return
            
        wf_data = resp.json()
        definition = wf_data.get("workflow_definition", {})
        
        # 2. Update the Webhook node
        updated = False
        for node in definition.get("nodes", []):
            # Check for both "webhook" and "http" types which are common in cloud versions
            if node.get("type") in ["webhook", "http", "api"]:
                print(f"Found {node['type']} node (ID: {node['id']}). Configuring Callback Identity...")
                node["data"]["endpoint_url"] = webhook_callback_url
                # FORCE PAYLOAD: Tell the cloud precisely what to send back
                node["data"]["payload"] = {
                    "lead_id": "{{initial_context.lead_id}}",
                    "lead_name": "{{initial_context.lead_name}}",
                    "phone_number": "{{phone_number}}",
                    "gathered_context": "{{gathered_context}}",
                    "workflow_run_id": "{{workflow_run_id}}"
                }
                updated = True
        
        if not updated:
            print("No webhook node found in workflow.")
            return

        # 3. Save the update
        print("Saving updated workflow...")
        save_resp = requests.put(
            f"{DOGRAH_API_BASE}/workflow/{WORKFLOW_ID}", 
            headers=headers,
            json={
                "name": wf_data["name"],
                "workflow_definition": definition
            }
        )
        
        if save_resp.status_code == 200:
            # 4. Publish the draft
            print("Publishing changes...")
            requests.post(f"{DOGRAH_API_BASE}/workflow/{WORKFLOW_ID}/publish", headers=headers)
            print(f"SUCCESS: Workflow {WORKFLOW_ID} now points to {webhook_callback_url}")
        else:
            print(f"Failed to save: {save_resp.text}")

    except Exception as e:
        print(f"Sync error: {e}")

def main():
    last_url = None
    print("Starting Synapse Tunnel Sync...")
    print(f"Monitoring logs for Workflow {WORKFLOW_ID}...")
    
    while True:
        current_url = get_latest_tunnel_url()
        
        if current_url and current_url != last_url:
            print(f"\nNEW TUNNEL DETECTED: {current_url}")
            if update_env(current_url):
                print("Updated .env file.")
            
            creds = get_dograh_creds()
            if creds["key"]:
                update_dograh_workflow(current_url, creds["key"])
            else:
                print("Skipping Dograh update: No API key found in .env")
            
            last_url = current_url
        
        time.sleep(10)

if __name__ == "__main__":
    main()
