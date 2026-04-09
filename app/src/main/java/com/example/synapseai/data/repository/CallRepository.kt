package com.example.synapseai.data.repository

import com.example.synapseai.data.api.ApiConstants
import com.example.synapseai.data.api.RetrofitClient
import com.example.synapseai.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CallRepository {

    private val dograhApi = RetrofitClient.dograhApi
    private val fastApi = RetrofitClient.fastApi

    suspend fun triggerCall(
        phoneNumber: String,
        context: Map<String, String> = emptyMap()
    ): Result<DograhCallResponse> {
        return try {
            val request = DograhCallRequest(phoneNumber, context)
            val response = dograhApi.triggerOutboundCall(ApiConstants.DOGRAH_AGENT_ID, request)
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
        language: String = "english",
        retryCount: Int = 0
    ): Result<ScheduleCallResponse> {
        return try {
            val request = ScheduleCallRequest(phoneNumber, scheduledTime, language, retryCount)
            val response = fastApi.scheduleCall(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Schedule failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun healthCheck(): Result<HealthResponse> {
        return try {
            val response = fastApi.healthCheck()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Health check failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadKnowledge(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = fastApi.uploadKnowledge(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Upload failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
