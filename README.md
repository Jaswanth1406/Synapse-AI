# ☎️ Synapse-AI: Intelligent Tele-Calling Agent

![License](https://img.shields.io/badge/license-Apache%202.0-blue)
![Stack](https://img.shields.io/badge/stack-Next.js%20%7C%20FastAPI-green)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

---

## 1. Project Title & Tagline

Synapse-AI is an intelligent, AI-driven tele-calling system that autonomously initiates calls, speaks in a natural human-like voice, and updates your CRM with structured intent and call insights.

---

## 2. Problem Statement

Outbound tele-calling teams spend huge time and money manually dialing, repeating the same scripts, and updating CRMs after every conversation. Human agents get fatigued, quality varies from call to call, and it is very hard to scale consistent, high-quality conversations across thousands of leads. Synapse-AI turns this into a programmable, AI-driven agent that can call prospects, hold natural conversations, qualify intent, and write back rich context to the CRM. For the OreHack problem context, it specifically targets high-volume outbound calls while keeping them context-aware, compliant, and easy to measure end-to-end.

---

## 3. Features

| Feature                             | Description                                                                                                                                                                               |
| ----------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 🧠 "SalesBrain" Conversation Engine | Replaces static scripts with a dynamic LLM state-machine that intelligently navigates Discovery, Pitch, and Closing phases, even handling real-time interruptions and complex objections. |
| 📚 RAG-Powered Knowledge Base       | Instantly retrieves relevant FAQs and specific company data to enhance response accuracy without hallucinations.                                                                          |
| 🌍 Global & Regional Reach          | Multi-language support capable of understanding and conversing in English, regional languages (like Tamil), and code-switching (e.g., Hinglish/Tanglish).                                 |
| 🔄 Automated Lifecycle Management   | Built-in Campaign Module that automates retry logic for unanswered calls, dynamically scores leads based on intent, and perfectly syncs call transcripts directly to your CRM.            |
| 📊 Stunning UI Dashboard            | Manage campaigns, visualize intent conversion rates, and manually trigger live calls from a modern glassmorphism interface.                                                               |

---

## 4. Tech Stack

### Frontend

- ⚛️ **Next.js (React)** — Component-based UI framework
- 🎨 **Pure CSS** — Glassmorphism design system
- 🔐 **Better Auth** — Google Sign-In & Credentials management

### Backend & Cloud

- 🐍 **FastAPI (Python)** — Webhook ingestion & Intent routing
- 💾 **Neon DB** — Serverless Postgres database
- 🎙️ **Dograh Cloud** — Handles STT, TTS, and LLM Telephony voice engine
- 🔗 **ESPO CRM** — Customer relationship management integration via Webhooks/REST APIs

---

## 5. Project Structure

```
Synapse-AI/
├── backend/
│   ├── bridge/                        # Middleware tunnel for CRM -> AI communication
│   ├── scripts/                       # Utilities for staging Dograh flows & CRM leads
│   ├── main.py                        # FastAPI entry point, scheduling & intent routing
│   ├── crm_connector.py               # Legacy integration logic 
│   ├── docker-compose.yml             # Full-stack orchestrator
│   └── requirements.txt               # Python dependencies
│
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── dashboard/             # Main application interface and analytics view
│   │   │   └── ...                    # Next.js App Router structure
│   │   ├── lib/                       # Utility functions and Better Auth setup
│   ├── .env.example                   # Environment variable template
│   └── package.json                   # Node.js dependencies
│
└── README.md
```

> **Note:** The heavy CRM (`espocrm/`) and Telephony components (`dograh/`) are not checked into this repository. You will need to spin up your own local or cloud instances of these platforms to complete the stack.

---

## 6. Installation & Setup

### Prerequisites

- Node.js (v18+)
- Python (v3.10+)
- A Dograh Cloud Account ([app.dograh.com](https://app.dograh.com/))
- A free serverless Postgres DB from [Neon.tech](https://neon.tech/)

### 1. Clone & Environment Variables

```bash
git clone <repository_url>
cd Synapse-AI
```

Duplicate `.env.example` as `.env` in the `frontend/` folder and fill in the following:

```env
DATABASE_URL="your-neon-db-string"
BETTER_AUTH_SECRET="your-random-secret"
GOOGLE_CLIENT_ID="your-google-oauth-client-id"
GOOGLE_CLIENT_SECRET="your-google-oauth-client-secret"
NEXT_PUBLIC_DOGRAH_API_KEY="your-dograh-api-key"
DOGRAH_AGENT_ID="your-dograh-agent-id"
```

> **Note:** A `get_dograh_key.py` script may be provided to securely fetch your Dograh API Key via terminal.

### 2. Frontend Initialization (Next.js)

```bash
cd frontend
npm install
npm run dev
```

Frontend dashboard runs at `http://localhost:3000`

### 3. Backend Initialization (FastAPI)

Open a new terminal window inside the `backend` directory.

```bash
cd backend
python -m venv venv

# Windows
.\venv\Scripts\activate
# Mac/Linux
source venv/bin/activate

pip install -r requirements.txt
uvicorn main:app --reload
```

Backend webhook and scheduling server runs at `http://localhost:8000`

---

### 6.1 Full Containerized Stack Setup (EspoCRM + AI Bridge)

If you are running the entire system using the provided Docker environment instead of just the Next.js UI, follow this guide to get the full stack (EspoCRM, Bridge Service, and Dograh AI) up and running.

### 🚀 Prerequisites

Before you start, make sure you have the following installed:
- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)
- Python 3.10+ (for running scripts)

### 🛠️ Step 1: Configuration

1. **Rename the Environment File**:
   Locate the `.env` file in the `backend/` directory (use `.env.example` as a template). Ensure it has the correct API keys and configuration.

   ```env
   # Dograh API Credentials
   DOGRAH_API_URL=https://api.dograh.com
   DOGRAH_API_KEY=your_dograh_key
   AGENT_UUID=your_agent_uuid

   # EspoCRM Internal Credentials
   ESPO_URL=http://espocrm
   ESPO_API_KEY=your_espo_api_key

   # Tunneling (Cloudflare)
   BRIDGE_PUBLIC_URL=https://your-tunnel-url.trycloudflare.com
   ```

### 📦 Step 2: Start the Services

In your terminal, navigate to the project root and run:

```bash
docker compose up -d
```

This will pull and start all necessary containers:
- **Bridge Service**: Internal service for CRM → AI communication (Port 5000)
- **Infrastructure**: MySQL, Postgres, Redis, Minio, and Cloudflared.

> **⚠️ Important Notice**: The underlying `espocrm` and `dograh` services must be hosted externally or created as custom local instances using your own setups. This repo only provides the `bridge` and `frontend` logic to orchestrate them. Ensure your `.env` points to your configured instances!

### ⚡ Step 3: Initialization

Once the containers are healthy, you need to initialize the CRM-to-Dograh path.

1. **Install Python Dependencies**:
   ```bash
   pip install httpx requests loguru
   ```

2. **Run the Initialization Script**:
   This script sets up the telephony config and the AI workflow in your local Dograh instance.
   ```bash
   python scripts/init_dograh_advanced.py
   ```
   *Note: This script outputs an `API_KEY` and `WORKFLOW_ID`. Keep these for your records.*

### 🧪 Step 4: Testing the Flow

To test if everything is working, you can manually push a lead into the CRM, which will trigger the AI call.

1. **Open `scripts/push_lead.py`** and verify the `CRM_URL`.
2. **Run the script**:
   ```bash
   python scripts/push_lead.py
   ```

### 🩺 Monitoring & Troubleshooting

#### Check Bridge Logs
To see how the AI conversation and retries are progressing:
```bash
docker logs -f bridge
```

#### Check Bridge Health
You can check the bridge status via these endpoints:
- **Health**: `http://localhost:5000/health`
- **Active Calls**: `http://localhost:5000/status/active-calls` (shows numbers currently being called)
- **Retry Status**: `http://localhost:5000/status/retries` (shows how many times each lead was called)

#### Logic Overview
- **Retries**: If a lead doesn't answer or hangs up, the bridge retries every **1 minute**.
- **Completion**: A lead is only marked "Completed" in CRM if they speak at least **4-5 times** or stay on the call for **60+ seconds**.
- **Guard**: The system will never call the same number twice at the exact same time.

---

### 6.2 API Documentation (Backend API)

Synapse-AI provides a robust REST API for orchestrating AI calls, managing schedules, and retrieving deep lead intelligence.

### Base URL
`http://localhost:8000`

### Authentication
Most endpoints require the `X-User-ID` header for data isolation (multi-tenancy).

| Header | Description | Required |
| --- | --- | --- |
| `X-User-ID` | The session/user ID (e.g., from Google Auth) | Yes (for user-specific data) |

---

### 📞 Call Operations

#### 1. Instant Trigger
`POST /api/calls/trigger`
Dispatches a natural-language AI call immediately via Dograh AI.

- **Payload:**
```json
{
  "phone_number": "+919876543210",
  "lead_name": "Sanjay",
  "language": "en"
}
```
- **Response:** `200 OK` with `workflow_run_id`.

#### 2. Smart Schedule
`POST /api/calls/schedule`
Schedules a call at a specific IST time with automated retry logic.

- **Payload:**
```json
{
  "phone_number": "+91 89392 78994",
  "scheduled_time": "2024-10-15T10:30:00",
  "retry_count": 3
}
```
- **Response:** `{"status": "success", "id": "schedule-id", ...}`

#### 3. Manage Schedules
- `GET /api/calls/scheduled`: List all user-specific pending and completed schedules.
- `DELETE /api/calls/scheduled/{id}`: Cancel a pending scheduled task.

---

### 📊 Intelligence & Analytics

#### 1. Real-time Dashboard Metrics
`GET /api/analytics`
Returns Groq AI-enriched metrics including conversion rates, sentiment distribution, and lead quality.

- **Output Format:**
```json
{
  "total_runs": 84,
  "conversion_rate": 22.5,
  "lead_quality": [ { "name": "HOT", "value": 12 }, ... ],
  "sentiment_dist": [ { "name": "positive", "value": 45 }, ... ],
  "duration_stats": [ { "range": "30-60s", "count": 20 }, ... ]
}
```

#### 2. Groq AI Backfill
`POST /api/analytics/backfill`
Forces the system to re-analyze all historical transcripts using the Groq LLM engine to capture missing intelligence data.

#### 3. Data Export
`GET /api/analytics/csv`
Downloads a full database dump of all call records, transcripts, and AI summaries as a CSV file.

---

### 🔗 Lead & CRM Integration

#### 1. Batch Lead Creation
`POST /api/leads/create`
Pushes a batch of leads directly into the CRM/Bridge system.

- **Payload:**
```json
{
  "leads": [
    { "firstName": "John", "lastName": "Doe", "phoneNumber": "+123456789" }
  ]
}
```

#### 2. Dograh Webhook (Inbound)
`POST /api/webhooks/dograh`
The endpoint where Dograh AI sends the transcript and call status after completion.

#### 3. CRM Contact Listing
`GET /api/leads`

Returns a simplified list of contacts/leads fetched directly from EspoCRM, used by the Contacts view in the dashboard.

- **Query:** none
- **Response (example):**

```json
[
  {
    "id": "65f123ab1234567890abcd01",
    "name": "John Doe",
    "phoneNumber": "+123456789",
    "status": "In Process",
    "createdAt": "2024-10-10T09:35:21Z",
    "updatedAt": "2024-10-10T10:12:03Z"
  }
]
```

---

## 7. How It Works

### 7.1 Workflow Walkthrough

1. **Set up the Campaign:** Log into the Next.js Dashboard. The UI allows you to monitor analytics and total calls made.
2. **Trigger a Call:** Enter a phone number on the dashboard and click **Dial Now** or schedule it for a later time.
3. **Execution:** The Next.js client or Python backend issues a secure POST request to the Dograh Cloud Engine using your `AGENT_ID`.
4. **Real-time Interaction:** Dograh calls the user, running real-time STT and TTS across the RAG-enhanced LLM logic.
5. **Post-Call Processing:** Dograh emits a webhook to the Python FastAPI backend.
6. **Analysis & Retry:** The backend recalculates user sentiment (Interested vs. Not Interested), evaluates if the call dropped/was un-answered (triggering persistent retry logic), and pushes verified lead data securely to ESPO CRM.

---

## 8. Scalability

- **Concurrent calls:** Voice processing is offloaded to Dograh Cloud, so many outbound calls can run in parallel while the local FastAPI backend only handles lightweight webhooks and scheduling.
- **Growing data volume:** Call logs, transcripts, and campaign metadata are stored in Postgres (Neon DB), which can scale storage and read/write throughput as the number of calls and campaigns increases.
- **Deployment model:** The stack can run in local dev mode or as a Dockerised composition (bridge + CRM + infra) on any cloud VM or container platform, allowing horizontal scaling by simply adding more replicas.
- **Identified bottlenecks:** Practical limits come from external APIs (telephony provider rate limits, CRM API quotas) and LLM latency; these are isolated behind retry logic today and can be further decoupled with queues/workers in a production deployment.

---

## 9. Feasibility

- **Mature tooling:** The system uses mainstream, well-documented tools (Next.js/React, FastAPI, Neon Postgres, Dograh, EspoCRM), so there is no exotic or research-only dependency.
- **Straightforward infrastructure:** A single Postgres instance, one FastAPI service, and the Next.js frontend are enough to run the core experience; optional Docker Compose files bundle them with CRM and bridge services.
- **Production hardening path:** To take this to production you would add proper logging/monitoring, background workers for long-running tasks, a secrets manager for API keys, and cloud deployment (e.g., Docker on a managed VM or container service).
- **Incremental rollout:** Teams can start by using only the outbound-calling and analytics APIs while keeping their existing CRM, then progressively switch to deeper integrations as needed.

---

## 10. Novelty

Unlike traditional robo-dialers that just play a recorded audio file, this architecture provides a low-latency, dynamic conversational loop powered by GenAI and Retrieval-Augmented Generation. The system not only calls and talks, but also understands user intent, tags it in the CRM, and drives a self-correcting retry loop for unanswered or short calls. The tight coupling between AI conversation, structured analytics, and CRM workflows is what differentiates Synapse-AI from simple outbound dialers.

---

## 11. Feature Depth

- **Conversational AI engine:** Handles interruptions, multi-turn context, discovery → pitch → closing phases, and gracefully exits when the user is not interested.
- **Outcome and retry logic:** Distinguishes completed, busy, no-answer, and failed calls, then schedules retries every few minutes with guards to avoid duplicate concurrent calls to the same number.
- **Campaign analytics:** Produces aggregated metrics like conversion rate, sentiment distribution, call-duration buckets, and lead-quality segments that can be exported as CSV.
- **Multilingual and regional support:** Extends reach across English and regional languages (e.g., Tamil, Hindi, code-switching) without changing the core UX.
- **Operator dashboard:** The glassmorphism UI is not just cosmetic; it exposes call stats, campaign health, and manual override controls so a human can step in when necessary.

---

## 12. Ethical Use & Disclaimer

Synapse-AI is strictly for **authorized business operations, lead generation, and customer support only**.

Do **NOT** use this software to spam, harass, or attempt phishing operations on unsuspecting users. Ensure compliance with regional telecommunication laws (e.g., TCPA in the US, GDPR in Europe, TRAI regulations in India) and DND (Do Not Disturb) registries.

Use responsibly and ethically.

---

## 13. License

Licensed under the [Apache 2.0 License](LICENSE).

---

### Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit your changes: `git commit -m "Add feature-name"`
4. Push and open a Pull Request

---

## 14. Author

- **Name:** Jaswanth
- **GitHub:** [github.com/Jaswanth1406](https://github.com/Jaswanth1406)

