# Vedaspark AI: Intelligent Tele-Calling Agent ☎️🤖

An intelligent, AI-driven tele-calling system that autonomously initiates calls, communicates naturally using human-like voice interaction, and dynamically responds to user queries based on business context. Designed to drastically reduce operational costs and eliminate human fatigue while scaling outbound outreach infinitely.

## 🚀 Key Features

* **"SalesBrain" Conversation Engine:** Replaces static scripts with a dynamic LLM state-machine that intelligently navigates Discovery, Pitch, and Closing phases, even handling real-time interruptions and complex objections (e.g., “Too expensive”, “Call me later”).
* **RAG-Powered Knowledge Base:** Instantly retrieves relevant FAQs and specific company data to enhance response accuracy without hallucinations.
* **Global & Regional Reach:** Multi-language support capable of understanding and conversing in English, regional languages (like Tamil), and code-switching (e.g., Hinglish/Tanglish).
* **Automated Lifecycle Management:** Built-in Campaign Module that automates retry logic for unanswered calls, dynamically scores leads based on intent, and perfectly syncs call transcripts directly to your CRM.
* **Stunning UI Dashboard:** Manage campaigns, visualize intent conversion rates, and manually trigger live calls from a modern glassmorphism interface.

## 🛠️ Tech Stack

* **Frontend:** Next.js (React), Pure CSS (Glassmorphism design)
* **Authentication:** Better Auth (Google Sign-In & Credentials)
* **Backend:** Python / FastAPI (Webhook ingestion & Intent routing)
* **Database:** Neon DB & Prisma ORM (Serverless Postgres)
* **Voice Agent Engine:** Dograh Cloud (Handles STT, TTS, and LLM Telephony)
* **CRM Integration:** ESPO CRM (Via Webhooks/REST APIs)

---

## 💻 Local Setup & Installation

### Prerequisites
* Node.js (v18+)
* Python (v3.10+)
* A Dograh Cloud Account ([app.dograh.com](https://app.dograh.com/))
* A free serverless Postgres DB from [Neon.tech](https://neon.tech/)

### 1. Clone & Environment Variables
First, clone the repository. Then duplicate `.env.example` as `.env` in the `frontend/` folder.
Fill in the following:
```env
DATABASE_URL="your-neon-db-string"
BETTER_AUTH_SECRET="your-random-secret"
GOOGLE_CLIENT_ID="your-google-oauth-client-id"
GOOGLE_CLIENT_SECRET="your-google-oauth-client-secret"
NEXT_PUBLIC_DOGRAH_API_KEY="your-dograh-api-key"
DOGRAH_AGENT_ID="your-dograh-agent-id"
```

> **Note:** We provided a `get_dograh_key.py` script in the root directory to help you securely fetch your Dograh API Key via terminal.

### 2. Frontend Initialization (Next.js)
```bash
cd frontend
npm install
npx prisma db push
npm run dev
```
*The frontend dashboard will be running at `http://localhost:3000`.*

### 3. Backend Initialization (FastAPI)
Open a new terminal window inside the `backend` directory.
```bash
cd backend
python -m venv venv

# Windows
.\venv\Scripts\activate
# Mac/Linux
source venv/bin/activate

pip install -r requirements.txt # (or install fastapi uvicorn pydantic requests)
uvicorn main:app --reload
```
*The webhook agent server will be running on `http://localhost:8000`.*

---

## 🔄 Workflow Walkthrough

1. **Set up the Campaign:** Log into the Next.js Dashboard. The UI allows you to monitor analytics and total calls made. 
2. **Trigger a Call:** Enter a phone number on the dashboard and click **Dial Now**.
3. **Execution:** The Next.js client issues a secure POST request to the Dograh Cloud Engine using your `AGENT_ID`.
4. **Real-time Interaction:** Dograh calls the user, running real-time STT and TTS across the RAG-enhanced LLM logic.
5. **Post-Call Processing:** Dograh emits a webhook to the Python FastAPI backend, which recalculates user sentiment (Interested vs. Not Interested) and pushes the verified lead data to ESPO CRM.

---
*Built for the Hackathon by the Synapse-AI Team.*