package com.example.synapseai.data.model

import com.google.gson.annotations.SerializedName

// ── Dograh Cloud Models ──

data class DograhCallRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("initial_context") val initialContext: Map<String, String> = emptyMap()
)

data class DograhCallResponse(
    @SerializedName("call_id") val callId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null
)

// ── FastAPI Models ──

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("service") val service: String
)

data class ScheduleCallRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("scheduled_time") val scheduledTime: String,
    @SerializedName("language") val language: String = "english",
    @SerializedName("retry_count") val retryCount: Int = 0
)

data class ScheduleCallResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("scheduled_time") val scheduledTime: String? = null
)

data class UploadResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("file_name") val fileName: String? = null
)

// ── ESPO CRM Models ──

data class EspoLeadListResponse(
    @SerializedName("list") val list: List<EspoLead> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

data class EspoLead(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("emailAddress") val emailAddress: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("accountName") val company: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class EspoCallListResponse(
    @SerializedName("list") val list: List<EspoCallLog> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

data class EspoCallLog(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("direction") val direction: String? = null,
    @SerializedName("dateStart") val dateStart: String? = null,
    @SerializedName("dateEnd") val dateEnd: String? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("description") val transcript: String? = null,
    @SerializedName("parentName") val parentName: String? = null
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

// ── Campaign Config Model ──

data class CampaignConfig(
    val objective: CampaignObjective = CampaignObjective.BOOK_MEETING,
    val tone: AgentTone = AgentTone.PROFESSIONAL,
    val language: AgentLanguage = AgentLanguage.ENGLISH
)

enum class CampaignObjective(val label: String, val description: String) {
    BOOK_MEETING("Book a Meeting", "Schedule a product demo or consultation"),
    GAUGE_INTEREST("Gauge Interest", "Assess lead's interest level in the product"),
    SEND_LINK("Send a Link", "Share relevant product/service information")
}

enum class AgentTone(val label: String) {
    PROFESSIONAL("Professional"),
    FRIENDLY("Friendly"),
    URGENT("Urgent"),
    CONSULTATIVE("Consultative")
}

enum class AgentLanguage(val label: String, val code: String) {
    ENGLISH("English", "english"),
    HINDI("Hindi", "hindi"),
    TAMIL("Tamil", "tamil")
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
