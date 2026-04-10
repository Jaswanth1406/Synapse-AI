import requests

CRM_URL = "https://be58-106-195-36-89.ngrok-free.app" 
API_KEY = "22dfa1554b34ec6158373dd53af1961b"

def push_lead(first_name, last_name, phone):
    url = f"{CRM_URL}/api/v1/Lead"
    headers = {
        "X-Api-Key": API_KEY,
        "Content-Type": "application/json"
    }
    payload = {
        "firstName": first_name,
        "lastName": last_name,
        "phoneNumber": phone
    }
    
    print(f"Pushing Lead {first_name} {last_name}...")
    try:
        resp = requests.post(url, json=payload, headers=headers)
        if resp.status_code in [200, 201]:
            print("SUCCESS: Lead added. AI Call will trigger automatically!")
        else:
            print(f"FAILED: {resp.status_code} - {resp.text}")
    except Exception as e:
        print(f"Error connecting to CRM: {e}")

if __name__ == "__main__":
    # Example usage
    push_lead("Sanjay", "Kumar", "+918939278994")
