# 🧪 Test Cases — Synapse Voice Automation

This document outlines 100 test cases covering edge cases, recovery mechanisms, and core logic for the Synapse Voice Bridge.

---

## 📽️ Demo Video
### [Watch the Full System Demo](https://drive.google.com/file/d/1oxidQuqjsn9Bhrex6WEa7q8qUqypZ9wm/view?usp=drive_link)

---

## 🏗️ 1. Lead Intake & Initial Trigger (1-10)
| ID | Scenario | Expected Result |
|---|---|---|
| 001 | Direct CRM Webhook with valid data | Call triggers immediately. |
| 002 | CRM Watcher finds a "New" lead | Call triggers within 30s. |
| 003 | Lead has missing phone number | Bridge logs error; skips call; no crash. |
| 004 | Lead has name with special characters | Call triggers; name passed correctly to Dograh. |
| 005 | Multiple leads hit Webhook simultaneously | Bridge handles concurrent requests via FastAPI ASGI. |
| 006 | Webhook payload missing `lead_id` | Log recovery attempt via phone lookup or skip. |
| 007 | Lead status changed back to "New" manually | CRM Watcher picks it up and re-starts workflow. |
| 008 | Lead phone number in wrong format (no +) | Bridge logs warning; Dograh likely fails (handled). |
| 009 | CRM API is down during Watcher poll | Bridge retries in next 30s cycle; no crash. |
| 010 | Watcher finds 20+ leads at once | Bridge processes up to `maxSize: 20` and loops. |

---

## 🛡️ 2. Call Guard & Deduplication (11-20)
| ID | Scenario | Expected Result |
|---|---|---|
| 011 | Triggering two calls to same number simultaneously | Call Guard (Lock) blocks the 2nd attempt. |
| 012 | Webhook and Runner Poller return same result | Deduplication logic (`_processed`) skips the duplicate. |
| 013 | Call finishes; lock is released | Next call to same number is permitted immediately. |
| 014 | Call crashes; lock never explicitly released | Safety timeout (15 min) auto-releases the lock. |
| 015 | Lead pushed with same ID but different Call ID | Bridge treats as new call session; updates registry. |
| 016 | Registry file becomes corrupted/unreadable | Bridge starts with empty dict; no crash. |
| 017 | Registry contains old stale IDs | No impact; new IDs continue to map correctly. |
| 018 | Simultaneous callback from Dograh and poll-success | Only one CRM update occurs. |
| 019 | Lock requested for invalid/empty phone | Lock denied or handled gracefully. |
| 020 | Multiple Webhooks for same Lead ID (Lead update spam) | Call Guard prevents multiple active dials. |

---

## 📡 3. Telephony Connectivity & Dograh Failures (21-30)
| ID | Scenario | Expected Result |
|---|---|---|
| 021 | Dograh returns 401 Unauthorized | Bridge logs error; updates CRM to "No Answer" + failure note. |
| 022 | Dograh returns 500 Internal Error | Bridge updates CRM to "No Answer" + schedules retry. |
| 023 | Dograh Agent UUID is invalid | Bridge handles failure response; updates CRM. |
| 024 | Call rejected by carrier (Twilio balance zero) | Bridge receives failure; updates CRM to "No Answer". |
| 025 | Dograh takes >10s to respond to trigger | httpx timeout handled; bridge doesn't hang. |
| 026 | Twilio Trial calling unverified number | 400 error from Dograh/Twilio handled; CRM updated. |
| 027 | Workflow ID does not exist | Bridge marks "No Answer" + failure note. |
| 028 | Dograh triggered but no Call ID in response | Bridge maps via Run ID (internal identity). |
| 029 | Internet out on Bridge PC during trigger | Request fails; retry triggered next cycle. |
| 030 | CRM URL is unreachable | Bridge logs error; retries update on next callback/poll. |

---

## 🗣️ 4. Conversation Quality & Parsing (31-45)
| ID | Scenario | Expected Result |
|---|---|---|
| 031 | 0 user replies, 10s duration | Marked "No Answer" or "Incomplete". |
| 032 | 1 user reply, 65s duration | Marked "Completed" (Meaningful). |
| 033 | 5 user replies, 20s duration | Marked "Completed" (Meaningful). |
| 034 | User hangs up after AI's first word | Marked "Incomplete". |
| 035 | AI transcript uses "Human" instead of "User" | Parser recognizes synonym; counts replies. |
| 036 | Transcript has Dograh timestamps `[00:00]` | Regex parser correctly extracts speaker and content. |
| 037 | User says "Not interested" (low reply count) | Marked "Incomplete". |
| 038 | User says "I am interested" (meaningful) | Marked "Converted". |
| 039 | Transcript URL is `transcripts/123.txt` (local) | Logic skips fetch; logs warning; uses fallback count. |
| 040 | Transcript fetch returns 404 | Bridge uses gathered_context/messages fallback. |
| 041 | Call duration is 0 from Dograh callback | Analyzed as "No Answer". |
| 042 | Assistant speaks 10 times, User speaks 0 | 0 User replies; Incomplete/No Answer. |
| 043 | Transcript is empty array `[]` | Uses `gathered_context` messages or summary. |
| 044 | User replies have multiple colons `user: Time: 10am` | Split logic handles content with colons correctly. |
| 045 | Metadata contains `call_disposition: user_hangup` | Handled as specific case for quality analysis. |

---

## 🕵️ 5. Identity Recovery & Callback Fallback (46-60)
| ID | Scenario | Expected Result |
|---|---|---|
| 046 | Webhook arrives with valid `run_id` | Lead ID looked up in Registry; CRM updated. |
| 047 | Webhook fails (Ngrok down) but Poller succeeds | Lead identity recovered via Registry; CRM updated. |
| 048 | Bridge restarts; Registry JSON reloaded | Identity mapping persists across container restarts. |
| 049 | Callback has only `phone_number` (no ID) | Bridge finds Lead ID via EspoCRM phone search. |
| 050 | Registry has multiple leads for same number | Bridge uses most recent mapping. |
| 051 | Run ID mapping exists but Lead was deleted in CRM | Bridge logs error 404 from CRM; cleans up registry. |
| 052 | Poller runs 20 times (5 mins) without result | Poller times out; schedules "No Answer" retry. |
| 053 | Dograh API down during Poller fetch | Poller continues to next interval. |
| 054 | Registry JSON is write-protected | Logs error; continues in-memory (limited persistence). |
| 055 | Run ID lookup fails; uses initial_context fallback | Identity recovered from Dograh payload data. |
| 056 | Callback arrives with unknown `call_id` | Bridge logs "unknown call"; identity recovery logic runs. |
| 057 | Multiple workflows running for same Lead | Run-specific IDs prevent cross-over confusion. |
| 058 | Poller finds `is_completed: false` | Poller sleeps and retries. |
| 059 | Dograh URL redirect (302) on Transcript | `httpx` follows redirect; transcript parsed. |
| 060 | Transcript is JSON instead of Text | Bridge detects JSON and parses `messages` array. |

---

## 🔄 6. Retry Logic & Timing (61-75)
| ID | Scenario | Expected Result |
|---|---|---|
| 061 | Call results in "No Answer" (Attempt 1) | Retry #1 scheduled for +60s. |
| 062 | Call results in "No Answer" (Attempt 2) | Retry #2 scheduled for +180s (3m). |
| 063 | Call results in "No Answer" after 2 retries today | Stops retrying; Watcher picks up tomorrow. |
| 064 | Lead is "Incomplete" (but user spoke) | No retry triggered (as per logic). |
| 065 | Attempt count resets at Midnight IST | Lead gets 2 fresh attempts next day. |
| 066 | Lead marked "Dead" in old logic | (New logic: Daily limit reached note created). |
| 067 | CRM Note contains IST timestamp | Date/Time matches India timezone correctly. |
| 068 | Retry store file is empty | Logic starts attempt count at 0. |
| 069 | Bridge killed during `asyncio.sleep` retry | Retry lost; CRM Watcher re-triggers in 30s. |
| 070 | Lead status changed from "No Answer" to "Converted" | Retries cleared; no more auto-dials. |
| 071 | Retry triggered but failed to trigger call | Logic logs error; stays in "No Answer" for next poll. |
| 072 | "No Answer" lead manual status change back to "New" | Daily counter resets/continues (handled by Watcher). |
| 073 | Retry scheduled but Lead ID removed from Registry | Identity recovery logic finds phone in CRM. |
| 074 | Retry #1 successful (Meaningful) | CRM set to Completed; Retry counter cleared. |
| 075 | 5 retries reached (as per 10:37 request) | Marked as daily limit hit; note created. |

---

## 🏠 7. CRM Updates & Statuses (76-85)
| ID | Scenario | Expected Result |
|---|---|---|
| 076 | UI shows Lead "Incomplete" | Note exists explaining user reply count vs required. |
| 077 | UI shows Lead "Converted" | Note exists highlighting AI summary interest. |
| 078 | Note post fails (403 Permission) | Bridge logs error; Status update still attempted. |
| 079 | CRM Lead has duplicate phone entries | Search returns list; Bridge uses index 0. |
| 080 | Note text too long for CRM field | (Handled by httpx/CRM; Bridge sends up to 2000 chars). |
| 081 | CRM Status change from "New" → "No Answer" | Webhook/Watcher loop avoids re-calling immediate Fail. |
| 082 | CRM Note shows IST time for manual verify | Matches user system time exactly (+5:30). |
| 083 | Note includes Markdown link to Transcript | Link is clickable in EspoCRM notes section. |
| 084 | Lead Assigned User changed during call | No effect on automation; update still works. |
| 085 | CRM Status updated but Note failed | Lead status is truth; logic continues correctly. |

---

## 💾 8. Data & Infrastructure (86-90)
| ID | Scenario | Expected Result |
|---|---|---|
| 086 | Docker Bridge container healthcheck | `/health` returns 200 OK. |
| 087 | Cloudflare Tunnel token expires | Calls fail; system waits for tunnel restore. |
| 088 | Bridge running under heavy load (100+ leads) | Asyncio queue handles processing linearly/concurrently. |
| 089 | Disk full on bridge (cannot write transcripts) | Logs error; CRM updates still proceed (API calls). |
| 090 | .env variables missing | Bridge fails to start; identifies missing VAR. |

---

## 🛡️ 9. Edge Conversation Cases (91-95)
| ID | Scenario | Expected Result |
|---|---|---|
| 091 | User speaks in non-English | Transcript captured; analysis depends on reply count. |
| 092 | User says "Shut up" and hangs up | Analyzed as "Incomplete". |
| 093 | Call lasts 10 minutes but user spoke once | Duration > 60s → Meaningful ("Completed"). |
| 094 | Bot-to-bot interaction | High reply count → "Completed". |
| 095 | User picks up but stays silent | 0 Replies; Duration > 5s → "No Answer" or "Incomplete". |

---

## 🧩 10. Manual Controls (96-100)
| ID | Scenario | Expected Result |
|---|---|---|
| 096 | `docker compose stop bridge` | All automation pauses; no new calls. |
| 097 | `docker compose start bridge` | Watcher runs immediately; resume pending tasks. |
| 098 | Manual `push_lead.py` trigger | Call starts; system processes normally. |
| 099 | Deleting `retries.json` | All retry counters reset to 0; fresh day. |
| 100 | Deleting `call_registry.json` | Active calls might lose identity recovery for 5 mins. |

---

### 🎥 [Demo Video Link](https://drive.google.com/file/d/1oxidQuqjsn9Bhrex6WEa7q8qUqypZ9wm/view?usp=drive_link)