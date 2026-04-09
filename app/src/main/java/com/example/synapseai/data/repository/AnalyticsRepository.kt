package com.example.synapseai.data.repository

import com.example.synapseai.data.model.EspoCallLog

class AnalyticsRepository {

    fun computeMetrics(callLogs: List<EspoCallLog>): AnalyticsMetrics {
        val totalCalls = callLogs.size
        val answered = callLogs.count { it.status?.lowercase() == "held" || it.duration != null && it.duration > 0 }
        val answerRate = if (totalCalls > 0) (answered.toFloat() / totalCalls * 100) else 0f

        val interested = callLogs.count {
            it.transcript?.lowercase()?.contains("interested") == true &&
                    it.transcript.lowercase().contains("not interested").not()
        }
        val notInterested = callLogs.count {
            it.transcript?.lowercase()?.contains("not interested") == true
        }
        val callback = callLogs.count {
            it.transcript?.lowercase()?.contains("callback") == true ||
                    it.transcript?.lowercase()?.contains("call back") == true
        }
        val conversionRate = if (totalCalls > 0) (interested.toFloat() / totalCalls * 100) else 0f
        val activeLeads = interested + callback

        return AnalyticsMetrics(
            totalCalls = totalCalls,
            answeredCalls = answered,
            answerRate = answerRate,
            interestedCount = interested,
            notInterestedCount = notInterested,
            callbackCount = callback,
            conversionRate = conversionRate,
            activeLeads = activeLeads
        )
    }
}

data class AnalyticsMetrics(
    val totalCalls: Int = 0,
    val answeredCalls: Int = 0,
    val answerRate: Float = 0f,
    val interestedCount: Int = 0,
    val notInterestedCount: Int = 0,
    val callbackCount: Int = 0,
    val conversionRate: Float = 0f,
    val activeLeads: Int = 0
)
