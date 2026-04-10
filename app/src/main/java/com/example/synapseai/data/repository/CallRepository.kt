package com.example.synapseai.data.repository

import com.example.synapseai.data.api.RetrofitClient
import com.example.synapseai.data.model.*

class CallRepository {

    private val api = RetrofitClient.api

    suspend fun triggerCall(
        phoneNumber: String,
        leadName: String = "Unknown",
        leadId: String = "undefined",
        language: String = "en"
    ): Result<TriggerCallResponse> {
        return try {
            val request = TriggerCallRequest(
                phoneNumber = phoneNumber,
                leadName = leadName,
                leadId = leadId,
                language = language,
                userId = UserIdConstants.MASTER_USER_ID
            )
            val response = api.triggerCall(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Call trigger failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scheduleCall(
        phoneNumber: String,
        scheduledTime: String,
        leadName: String = "Unknown",
        language: String = "en",
        retryCount: Int = 3
    ): Result<ScheduleCallResponse> {
        return try {
            val request = ScheduleCallRequest(
                phoneNumber = phoneNumber,
                scheduledTime = scheduledTime,
                leadName = leadName,
                language = language,
                retryCount = retryCount,
                userId = UserIdConstants.MASTER_USER_ID
            )
            val response = api.scheduleCall(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Schedule failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCallHistory(): Result<List<CallHistoryEntry>> {
        return try {
            val response = api.getCallHistory(UserIdConstants.MASTER_USER_ID)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("History fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchScheduledCalls(): Result<List<ScheduledCallEntry>> {
        return try {
            val response = api.getScheduledCalls(UserIdConstants.MASTER_USER_ID)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Scheduled calls fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelScheduledCall(id: String): Result<GenericApiResponse> {
        return try {
            val response = api.cancelScheduledCall(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Cancel failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun healthCheck(): Result<HealthResponse> {
        return try {
            val response = api.healthCheck()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Health check failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
