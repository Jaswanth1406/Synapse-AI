package com.example.synapseai.data.repository

import com.example.synapseai.data.api.RetrofitClient
import com.example.synapseai.data.model.AnalyticsResponse
import com.example.synapseai.data.model.BackfillResponse
import com.example.synapseai.data.model.UserIdConstants
import okhttp3.ResponseBody

class AnalyticsRepository {

    private val api = RetrofitClient.api

    suspend fun fetchAnalytics(): Result<AnalyticsResponse> {
        return try {
            val response = api.getAnalytics(UserIdConstants.MASTER_USER_ID)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Analytics fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun triggerBackfill(): Result<BackfillResponse> {
        return try {
            val response = api.triggerBackfill()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Backfill failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadCsv(): Result<ResponseBody> {
        return try {
            val response = api.downloadCsv(UserIdConstants.MASTER_USER_ID)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("CSV download failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
