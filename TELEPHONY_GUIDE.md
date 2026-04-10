# 📞 Telephony Provider Guide — Synapse Project

This guide covers how to set up and configure telephony providers for Dograh AI voice calls in the Synapse project.

---

## Current Setup

Synapse uses **Dograh AI** to make outbound voice calls. Dograh connects to a telephony provider (currently **Twilio**) to actually place the calls. The flow is:

```
CRM Lead → Bridge → Dograh API → Twilio → Phone Call → Callback → Bridge → CRM Update
```

> **Important**: The telephony provider is configured **inside Dograh's platform**, not in the bridge code. The bridge only talks to Dograh's API.

---

## Option 1: Twilio (Current)

### What is Twilio?
Twilio is the most popular cloud telephony platform. Dograh uses it by default.

### Setup Steps

#### 1. Create a Twilio Account
- Go to [twilio.com/try-twilio](https://www.twilio.com/try-twilio)
- Sign up with your email and verify your phone number

#### 2. Get Your Twilio Credentials
After signing up, find these in the [Twilio Console](https://console.twilio.com/):
- **Account SID** — starts with `AC...`
- **Auth Token** — click the eye icon to reveal
- **Phone Number** — buy one from the console ($1/month)

#### 3. Buy a Phone Number
```
Console → Phone Numbers → Buy a Number → Select a US number with Voice capability
```
Cost: ~$1.00/month + $0.014/min for outbound calls

#### 4. Configure in Dograh
1. Log into [Dograh Dashboard](https://app.dograh.com)
2. Go to **Settings → Integrations → Twilio**
3. Enter your **Account SID**, **Auth Token**, and **Phone Number**
4. Save and test

#### 5. Trial Account Limitations
> ⚠️ **With a Twilio Trial account, you can ONLY call verified numbers.**

To verify a number:
```
Console → Phone Numbers → Verified Caller IDs → Add a new number
```

To remove this restriction: **Upgrade to a paid account** ($20 minimum top-up).

### Twilio Pricing
| Item | Cost |
|---|---|
| Phone Number | $1.00/month |
| Outbound Calls (US) | $0.014/min |
| Outbound Calls (India) | $0.0225/min |
| Account Upgrade | $20.00 one-time |

### Twilio Troubleshooting

| Error | Cause | Fix |
|---|---|---|
| `21219 - To number not verified` | Trial account restriction | Verify the number or upgrade account |
| `21614 - Not a valid phone number` | Wrong phone format | Use E.164 format: `+918939278994` |
| `21217 - Inactive account` | Account suspended | Add funds to your Twilio account |
| `13224 - Dial: Invalid phone number` | Number can't be reached | Verify the number is dialable |

---

## Option 2: Telnyx (Recommended Alternative)

### Why Telnyx?
- **No trial restrictions** — call any number immediately
- **Cheaper rates** — especially for India calls
- **Better call quality** — uses private IP network
- **Mission Control Portal** — easier to manage

### Setup Steps

#### 1. Create Account
- Go to [telnyx.com/sign-up](https://telnyx.com/sign-up)
- Verify your email and identity

#### 2. Get API Key
```
Portal → API Keys → Create API Key → Copy the key
```

#### 3. Buy a Number
```
Portal → Numbers → Search & Buy → Select a number with Voice capability
```
Cost: ~$1.00/month

#### 4. Create a SIP Connection
```
Portal → SIP Connections → Create → Set to "Credentials" type
```

#### 5. Configure in Dograh
1. Dograh Dashboard → **Settings → Integrations**
2. Select **Telnyx** as provider (if available)
3. Enter your **API Key** and **Phone Number**
4. If Dograh doesn't have native Telnyx support, use **SIP Trunking**:
   - In Telnyx: Create an Outbound Voice Profile
   - In Dograh: Configure SIP trunk with Telnyx credentials

### Telnyx Pricing
| Item | Cost |
|---|---|
| Phone Number | $1.00/month |
| Outbound Calls (US) | $0.007/min |
| Outbound Calls (India) | $0.015/min |
| Account Minimum | $10.00 |

---

## Option 3: Plivo

### Why Plivo?
- **Competitive pricing** — cheapest for high volume
- **Good India coverage** — reliable for Indian numbers
- **Simple API** — similar to Twilio but less complex

### Setup Steps

#### 1. Create Account
- Go to [plivo.com/sign-up](https://www.plivo.com/sign-up/)

#### 2. Get Credentials
```
Dashboard → Auth ID and Auth Token (shown on main page)
```

#### 3. Buy a Number
```
Dashboard → Phone Numbers → Buy Numbers
```

#### 4. Configure in Dograh
- Use SIP Trunking if Dograh doesn't have native Plivo support
- Plivo SIP endpoint: `sip.plivo.com`

### Plivo Pricing
| Item | Cost |
|---|---|
| Phone Number | $0.80/month |
| Outbound Calls (US) | $0.010/min |
| Outbound Calls (India) | $0.015/min |
| Trial Calls | Free (with Plivo branding) |

---

## Option 4: Vonage (Nexmo)

### Why Vonage?
- **Global reach** — 200+ countries
- **Good for SMS + Voice** — combined platform
- **Free trial credits** — $2 free credit on signup

### Setup Steps

#### 1. Create Account
- Go to [dashboard.nexmo.com/sign-up](https://dashboard.nexmo.com/sign-up)

#### 2. Get Credentials
```
Dashboard → API Key and API Secret
```

#### 3. Buy a Number
```
Dashboard → Numbers → Buy Numbers
```

#### 4. Configure in Dograh
- Use SIP Trunking with Vonage SIP endpoint

### Vonage Pricing
| Item | Cost |
|---|---|
| Phone Number | $0.67/month |
| Outbound Calls (US) | $0.014/min |
| Outbound Calls (India) | $0.030/min |
| Free Trial Credit | $2.00 |

---

## Quick Comparison

| Feature | Twilio | Telnyx | Plivo | Vonage |
|---|---|---|---|---|
| **India Call Rate** | $0.0225/min | $0.015/min | $0.015/min | $0.030/min |
| **US Call Rate** | $0.014/min | $0.007/min | $0.010/min | $0.014/min |
| **Trial Restrictions** | Verified numbers only | None | Branding on calls | $2 credit |
| **Min Top-up** | $20 | $10 | $10 | $0 |
| **Dograh Integration** | ✅ Native | 🔧 SIP Trunk | 🔧 SIP Trunk | 🔧 SIP Trunk |
| **Best For** | Easy setup | Best value | High volume | Global reach |

> **Recommendation**: If Twilio trial restrictions are blocking you, switch to **Telnyx** for the cheapest rates and no trial limitations. If you need native Dograh support, upgrade your **Twilio to a paid account** ($20).

---

## How to Switch Providers in Dograh

### Method 1: Native Integration (Twilio)
```
Dograh Dashboard → Settings → Integrations → Twilio
Enter: Account SID, Auth Token, Phone Number
```

### Method 2: SIP Trunking (Telnyx/Plivo/Vonage)
1. Set up SIP credentials with your chosen provider
2. In Dograh Dashboard:
   ```
   Settings → Telephony → SIP Trunk
   SIP URI: sip:USERNAME@sip.provider.com
   Username: your_sip_username
   Password: your_sip_password
   Caller ID: +1XXXXXXXXXX (your purchased number)
   ```
3. Test with a known number first

### Method 3: Bring Your Own Carrier (BYOC)
If your provider supports BYOC with Twilio:
1. Set up BYOC trunk in Twilio Console
2. Route calls through your own carrier
3. Keeps Dograh's native Twilio integration working

---

## Source Code — Current (Twilio via Dograh)

The bridge currently triggers calls through Dograh's API, which internally uses Twilio. Here's the relevant code from `bridge/bridge.py`:

### `.env` (Twilio via Dograh)
```env
# Dograh API (uses Twilio internally)
DOGRAH_API_URL=https://api.dograh.com
DOGRAH_API_KEY=DOGRAH_API_KEY
AGENT_UUID=DOGRAH_AGENT_UUID
WORKFLOW_ID=DOGRAH_WORKFLOW_ID
```

### `bridge.py` — `trigger_call()` (Twilio via Dograh)
```python
async def trigger_call(lead_id, phone, name):
    if not await lock(phone):
        logger.warning(f"BLOCKED: {phone} already in progress")
        return None
    logger.info(f"CALLING {name} ({phone})")
    try:
        async with httpx.AsyncClient() as c:
            r = await c.post(
                f"{DOGRAH_API_URL}/api/v1/public/agent/{AGENT_UUID}",
                json={
                    "phone_number": phone,
                    "workflow_id": int(WORKFLOW_ID),
                    "initial_context": {
                        "lead_name": name,
                        "lead_id": lead_id,
                        "phone": phone
                    }
                },
                headers={"X-API-Key": DOGRAH_API_KEY}
            )
            if r.status_code not in (200, 201):
                logger.error(f"Call failed: {r.text}")
                await unlock(phone)
                await crm_update(lead_id, "No Answer")
                await crm_note(lead_id, f"**Call Failed**\n{r.text[:200]}")
                return None
            return r.json()
    except Exception as e:
        logger.error(f"Call error: {e}")
        await unlock(phone)
        return None
```

### `bridge.py` — `poll_result()` (Twilio via Dograh)
```python
async def poll_result(run_id, lead_id, phone, name):
    """Poll Dograh API for call completion."""
    for i in range(20):  # 20 × 15s = 5 min
        await asyncio.sleep(15)
        try:
            async with httpx.AsyncClient(timeout=10) as c:
                r = await c.get(
                    f"{DOGRAH_API_URL}/api/v1/workflow/{WORKFLOW_ID}/runs/{run_id}",
                    headers={"X-API-Key": DOGRAH_API_KEY}
                )
                if r.status_code != 200:
                    continue
                d = r.json()
                if not d.get("is_completed"):
                    continue
                # Extract duration from telephony callbacks
                dur = 0
                for cb in (d.get("logs", {}).get("telephony_status_callbacks") or []):
                    if cb.get("duration"):
                        dur = max(dur, int(cb["duration"]))
                await process_result({
                    "call_id": str(run_id),
                    "workflow_run_id": str(run_id),
                    "phone_number": phone,
                    "duration": str(dur),
                    "status": "completed",
                    "gathered_context": d.get("gathered_context", {}),
                    "transcript_url": d.get("transcript_url", ""),
                    "initial_context": {
                        "lead_name": name,
                        "lead_id": lead_id,
                        "phone": phone
                    }
                })
                return
        except Exception as e:
            logger.debug(f"POLL error: {e}")
    # Timeout — mark as No Answer
    await unlock(phone)
    await crm_update(lead_id, "No Answer")
    asyncio.create_task(do_retry(lead_id, phone, name))
```

---

## Source Code — Switching to Telnyx (Direct)

If you want to bypass Dograh and call directly via Telnyx, here are the code changes:

### Step 1: Install Telnyx SDK
Add to `bridge/requirements.txt`:
```
telnyx
```

### Step 2: Update `.env`
```env
# Replace Dograh vars with Telnyx
TELNYX_API_KEY=KEY_your_telnyx_api_key
TELNYX_PHONE_NUMBER=+12025551234
TELNYX_APP_ID=your_telnyx_app_id

# Keep CRM config
ESPO_URL=http://espocrm
ESPO_API_KEY=CRM_API_KEY
```

### Step 3: Replace `trigger_call()` in `bridge.py`
```python
import telnyx

# Config
TELNYX_API_KEY = os.getenv("TELNYX_API_KEY")
TELNYX_PHONE   = os.getenv("TELNYX_PHONE_NUMBER")
TELNYX_APP_ID  = os.getenv("TELNYX_APP_ID")
telnyx.api_key = TELNYX_API_KEY


async def trigger_call(lead_id, phone, name):
    if not await lock(phone):
        logger.warning(f"BLOCKED: {phone} already in progress")
        return None
    logger.info(f"CALLING {name} ({phone}) via Telnyx")
    try:
        call = telnyx.Call.create(
            connection_id=TELNYX_APP_ID,
            to=phone,
            from_=TELNYX_PHONE,
            webhook_url=f"{BRIDGE_PUBLIC_URL}/callback/telnyx",
            custom_headers=[
                {"name": "X-Lead-Id", "value": lead_id},
                {"name": "X-Lead-Name", "value": name}
            ]
        )
        return {"call_id": call.call_control_id, "status": "initiated"}
    except Exception as e:
        logger.error(f"Telnyx call error: {e}")
        await unlock(phone)
        await crm_update(lead_id, "No Answer")
        await crm_note(lead_id, f"**Call Failed**\n{str(e)[:200]}")
        return None
```

### Step 4: Add Telnyx callback handler in `bridge.py`
```python
@app.post("/callback/telnyx")
async def on_telnyx_callback(request: Request):
    data = await request.json()
    event = data.get("data", {})
    event_type = event.get("event_type", "")
    payload = event.get("payload", {})
    
    # We care about call.hangup and call.answered
    if event_type == "call.hangup":
        call_id = payload.get("call_control_id")
        phone = payload.get("to", "")
        duration = int(payload.get("duration_secs", 0))
        
        # Find lead from call registry
        lead_id = registry_get(call_id)
        if not lead_id:
            return {"status": "unknown_call"}
        
        await process_result({
            "call_id": call_id,
            "phone_number": phone,
            "duration": str(duration),
            "status": "completed" if duration > 0 else "no-answer",
            "transcript": [],
            "initial_context": {"lead_id": lead_id}
        })
    
    return {"status": "ok"}
```

### Step 5: Update `docker-compose.yml`
```yaml
bridge:
  environment:
    - TELNYX_API_KEY=${TELNYX_API_KEY}
    - TELNYX_PHONE_NUMBER=${TELNYX_PHONE_NUMBER}
    - TELNYX_APP_ID=${TELNYX_APP_ID}
    - BRIDGE_PUBLIC_URL=${BRIDGE_PUBLIC_URL}
```

> **Note**: With Telnyx direct, you lose Dograh's AI conversation features. This is only useful if you want raw telephony without the AI agent. For AI-powered calls, keep using Dograh and just switch Dograh's telephony provider to Telnyx via their SIP trunk settings.

---

## Source Code — Switching to Plivo (Direct)

### Step 1: Install Plivo SDK
Add to `bridge/requirements.txt`:
```
plivo
```

### Step 2: Update `.env`
```env
PLIVO_AUTH_ID=your_plivo_auth_id
PLIVO_AUTH_TOKEN=your_plivo_auth_token
PLIVO_PHONE_NUMBER=+12025551234
```

### Step 3: Replace `trigger_call()` in `bridge.py`
```python
import plivo

PLIVO_AUTH_ID    = os.getenv("PLIVO_AUTH_ID")
PLIVO_AUTH_TOKEN = os.getenv("PLIVO_AUTH_TOKEN")
PLIVO_PHONE      = os.getenv("PLIVO_PHONE_NUMBER")
plivo_client = plivo.RestClient(PLIVO_AUTH_ID, PLIVO_AUTH_TOKEN)


async def trigger_call(lead_id, phone, name):
    if not await lock(phone):
        logger.warning(f"BLOCKED: {phone} already in progress")
        return None
    logger.info(f"CALLING {name} ({phone}) via Plivo")
    try:
        response = plivo_client.calls.create(
            from_=PLIVO_PHONE,
            to_=phone,
            answer_url=f"{BRIDGE_PUBLIC_URL}/plivo/answer",
            hangup_url=f"{BRIDGE_PUBLIC_URL}/callback/plivo",
        )
        call_id = response["request_uuid"]
        registry_set(call_id, lead_id)
        return {"call_id": call_id, "status": "initiated"}
    except Exception as e:
        logger.error(f"Plivo call error: {e}")
        await unlock(phone)
        await crm_update(lead_id, "No Answer")
        return None
```

---

## Running the System

Once your telephony provider is configured:

```bash
# Start everything
docker compose up -d

# Watch bridge logs
docker logs -f bridge

# Push a test lead
cd scripts
python push_lead.py

# The bridge will:
# 1. Detect the new lead (CRM watcher polls every 30s)
# 2. Trigger call via configured provider
# 3. Poll for results / receive callback
# 4. Update CRM status with IST timestamp
# 5. Retry up to 2x/day if no answer, resume next day
```

### To switch between providers:
1. Update `.env` with new credentials
2. Update `trigger_call()` in `bridge.py` (code samples above)
3. Rebuild: `docker compose build --no-cache bridge && docker compose up -d bridge`

