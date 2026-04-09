package com.example.synapseai.data.api

import com.example.synapseai.data.model.HealthResponse
import com.example.synapseai.data.model.ScheduleCallRequest
import com.example.synapseai.data.model.ScheduleCallResponse
import com.example.synapseai.data.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FastApiService {

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>

    @POST("api/calls/schedule")
    suspend fun scheduleCall(
        @Body request: ScheduleCallRequest
    ): Response<ScheduleCallResponse>

    @Multipart
    @POST("api/knowledge/upload")
    suspend fun uploadKnowledge(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
}
