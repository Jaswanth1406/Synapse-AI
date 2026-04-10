# ☎️ Synapse-AI: Intelligent Tele-Calling Agent

> An intelligent, AI-driven tele-calling system that autonomously initiates calls, communicates naturally using human-like voice interaction, and dynamically responds to user queries based on business context. Designed to drastically reduce operational costs and eliminate human fatigue while scaling outbound outreach infinitely.

![License](https://img.shields.io/badge/license-Apache%202.0-blue)
![Stack](https://img.shields.io/badge/stack-Next.js%20%7C%20FastAPI-green)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

---

## 📌 Problem Statement

Automating human-like outbound tele-calling outreach to reduce operational costs, eliminate agent fatigue, and scale sales and support workflows infintely while dynamically syncing intent and lead data to CRM.

---

## 🚀 Features

| Feature                             | Description                                                                                                                                                                               |
| ----------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 🧠 "SalesBrain" Conversation Engine | Replaces static scripts with a dynamic LLM state-machine that intelligently navigates Discovery, Pitch, and Closing phases, even handling real-time interruptions and complex objections. |
| 📚 RAG-Powered Knowledge Base       | Instantly retrieves relevant FAQs and specific company data to enhance response accuracy without hallucinations.                                                                          |
| 🌍 Global & Regional Reach          | Multi-language support capable of understanding and conversing in English, regional languages (like Tamil), and code-switching (e.g., Hinglish/Tanglish).                                 |
| 🔄 Automated Lifecycle Management   | Built-in Campaign Module that automates retry logic for unanswered calls, dynamically scores leads based on intent, and perfectly syncs call transcripts directly to your CRM.            |
| 📊 Stunning UI Dashboard            | Manage campaigns, visualize intent conversion rates, and manually trigger live calls from a modern glassmorphism interface.                                                               |

---

## 🏗️ Tech Stack

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

## 📂 Project Structure

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

## ⚙️ Installation & Setup

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

## 🐳 Full Containerized Stack Setup (EspoCRM + AI Bridge)

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

## 🧠 How It Works

### Workflow Walkthrough

1. **Set up the Campaign:** Log into the Next.js Dashboard. The UI allows you to monitor analytics and total calls made.
2. **Trigger a Call:** Enter a phone number on the dashboard and click **Dial Now** or schedule it for a later time.
3. **Execution:** The Next.js client or Python backend issues a secure POST request to the Dograh Cloud Engine using your `AGENT_ID`.
4. **Real-time Interaction:** Dograh calls the user, running real-time STT and TTS across the RAG-enhanced LLM logic.
5. **Post-Call Processing:** Dograh emits a webhook to the Python FastAPI backend.
6. **Analysis & Retry:** The backend recalculates user sentiment (Interested vs. Not Interested), evaluates if the call dropped/was un-answered (triggering persistent retry logic), and pushes verified lead data securely to ESPO CRM.

---

## 📈 Scalability

* **Voice Engine** load is entirely offloaded to Dograh Cloud, allowing massively parallel simultaneous outbound calls without lagging the local server.
* **Backend processing** via FastAPI natively supports high concurrency and asynchronous webhook handling.
* **Serverless Edge DB (Neon)** handles relational storage scaling seamlessly without manual provisioning.

---

## 💡 Feasibility

Synapse-AI drastically reduces the barrier to entry for enterprise-grade outbound calling operations by leveraging robust platforms like Dograh for voice transport, Neon for serverless DB scaling, and minimal bespoke code footprint for orchestration. It fits seamlessly into existing business ecosystems via immediate REST API integration to ESPO CRM.

---

## 🌟 Novelty

Unlike traditional robo-dialers that just play a recorded audio file, this architecture provides a low-latency, dynamic conversational loop powered by GenAI and Retrieval-Augmented Generation. Furthermore, the inclusion of an intelligent self-correcting retry loop for unanswered leads and automatic robust CRM intent tagging removes the entire burden of manual follow-ups from human SDRs.

---

## 🔧 Feature Depth

* **Conversational AI** gracefully handles interruptions, multi-turn contexts, and dynamic discovery phases.
* **Detailed Call Statuses:** Captures granular outcomes—completed, busy, no-answer, failed—to intelligently determine retry cadences.
* **Multilingual support:** Extends reach seamlessly within regional demographics (Tamil, Hindi, Code-Switching).
* **Glassmorphic UI** offers a highly premium user engagement interface for campaign oversight.

---

## ⚠️ Ethical Use & Disclaimer

Synapse-AI is strictly for **authorized business operations, lead generation, and customer support only**.

Do **NOT** use this software to spam, harass, or attempt phishing operations on unsuspecting users. Ensure compliance with regional telecommunication laws (e.g., TCPA in the US, GDPR in Europe, TRAI regulations in India) and DND (Do Not Disturb) registries.

Use responsibly and ethically.

---

## 📜 License

Licensed under the [Apache 2.0 License](LICENSE).

---

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit your changes: `git commit -m "Add feature-name"`
4. Push and open a Pull Request

---

## 🧩 Author

**Synapse-AI Team**
*Built for the Hackathon.*
