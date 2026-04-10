package com.example.synapseai.data.model

import com.google.gson.annotations.SerializedName

// ── Master User ID for MVP ──
object UserIdConstants {
    const val MASTER_USER_ID = "synapse_mvp_2024"
}

// ── Call Trigger (POST /api/calls/trigger) ──

data class TriggerCallRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("lead_name") val leadName: String = "Unknown",
    @SerializedName("lead_id") val leadId: String = "undefined",
    @SerializedName("language") val language: String = "en",
    @SerializedName("user_id") val userId: String = UserIdConstants.MASTER_USER_ID
)

data class TriggerCallResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("call_data") val callData: CallData? = null
)

data class CallData(
    @SerializedName("status") val status: String? = null,
    @SerializedName("workflow_run_id") val workflowRunId: Long? = null,
    @SerializedName("workflow_run_name") val workflowRunName: String? = null
)

// ── Call Schedule (POST /api/calls/schedule) ──

data class ScheduleCallRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("scheduled_time") val scheduledTime: String,
    @SerializedName("lead_name") val leadName: String = "Unknown",
    @SerializedName("language") val language: String = "en",
    @SerializedName("retry_count") val retryCount: Int = 3,
    @SerializedName("user_id") val userId: String = UserIdConstants.MASTER_USER_ID
)

data class ScheduleCallResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("scheduled_time") val scheduledTime: String? = null
)

// ── Scheduled Calls List (GET /api/calls/scheduled) ──

data class ScheduledCallEntry(
    @SerializedName("id") val id: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("lead_name") val leadName: String? = null,
    @SerializedName("scheduled_time") val scheduledTime: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("retry_count") val retryCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)

// ── Call History (GET /api/calls/history) ──

data class CallHistoryEntry(
    @SerializedName("call_id") val callId: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("intent") val intent: String? = null,
    @SerializedName("transcript") val transcript: String? = null,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("recording_url") val recordingUrl: String? = null
)

// ── Analytics (GET /api/analytics) ──

data class AnalyticsResponse(
    @SerializedName("total_runs") val totalRuns: Int = 0,
    @SerializedName("conversion_rate") val conversionRate: Double = 0.0,
    @SerializedName("interested_count") val interestedCount: Int = 0,
    @SerializedName("callback_count") val callbackCount: Int = 0,
    @SerializedName("avg_engagement") val avgEngagement: Double = 0.0,
    @SerializedName("intent_breakdown") val intentBreakdown: List<ChartDataItem> = emptyList(),
    @SerializedName("lead_quality") val leadQuality: List<ChartDataItem> = emptyList(),
    @SerializedName("sentiment_dist") val sentimentDist: List<ChartDataItem> = emptyList(),
    @SerializedName("dispositions") val dispositions: List<ChartDataItem> = emptyList(),
    @SerializedName("duration_stats") val durationStats: List<DurationDataItem> = emptyList()
)

data class ChartDataItem(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: Int
)

data class DurationDataItem(
    @SerializedName("range") val range: String,
    @SerializedName("count") val count: Int
)

// ── Backfill (POST /api/analytics/backfill) ──

data class BackfillResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("total_candidates") val totalCandidates: Int = 0,
    @SerializedName("updated") val updated: Int = 0,
    @SerializedName("errors") val errors: Int = 0
)

// ── Health (GET /health) ──

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("service") val service: String
)

// ── Batch Lead Creation (POST /api/leads/create) ──

data class LeadEntry(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)

data class BatchLeadRequest(
    @SerializedName("leads") val leads: List<LeadEntry>,
    @SerializedName("user_id") val userId: String = UserIdConstants.MASTER_USER_ID
)

data class BatchLeadResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("created") val created: Int = 0
)

// ── Generic API Response ──

data class GenericApiResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null
)

// ── CRM Lead Listing (GET /api/leads) ──

data class CrmLeadEntry(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// ── Local Contact Model ──

data class Contact(
    val id: String = "",
    val name: String,
    val phoneNumber: String,
    val company: String = "",
    val leadStatus: LeadStatus = LeadStatus.PENDING,
    val lastCallDate: String? = null,
    val notes: String? = null
)

enum class LeadStatus(val label: String) {
    INTERESTED("Interested"),
    NOT_INTERESTED("Not Interested"),
    CALLBACK("Callback"),
    PENDING("Pending"),
    NO_ANSWER("No Answer")
}

// ── Call State for Dialer ──

enum class CallState(val label: String) {
    IDLE("Ready"),
    INITIATING("Initiating..."),
    CONNECTED("Connected"),
    SPEAKING("AI Speaking"),
    CONCLUDED("Call Concluded"),
    FAILED("Failed")
}
