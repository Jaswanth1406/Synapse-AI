package com.example.synapseai.data.api

import com.example.synapseai.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface FastApiService {

    // ── Health ──
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>

    // ── Call Trigger ──
    @POST("api/calls/trigger")
    suspend fun triggerCall(
        @Body request: TriggerCallRequest
    ): Response<TriggerCallResponse>

    // ── Call Schedule ──
    @POST("api/calls/schedule")
    suspend fun scheduleCall(
        @Body request: ScheduleCallRequest
    ): Response<ScheduleCallResponse>

    // ── Scheduled Calls List ──
    @GET("api/calls/scheduled")
    suspend fun getScheduledCalls(
        @Query("user_id") userId: String
    ): Response<List<ScheduledCallEntry>>

    // ── Cancel Scheduled Call ──
    @DELETE("api/calls/scheduled/{id}")
    suspend fun cancelScheduledCall(
        @Path("id") id: String
    ): Response<GenericApiResponse>

    // ── Call History ──
    @GET("api/calls/history")
    suspend fun getCallHistory(
        @Query("user_id") userId: String
    ): Response<List<CallHistoryEntry>>

    // ── Analytics ──
    @GET("api/analytics")
    suspend fun getAnalytics(
        @Query("user_id") userId: String
    ): Response<AnalyticsResponse>

    // ── Analytics Backfill ──
    @POST("api/analytics/backfill")
    suspend fun triggerBackfill(): Response<BackfillResponse>

    // ── CSV Export ──
    @Streaming
    @GET("api/analytics/csv")
    suspend fun downloadCsv(
        @Query("user_id") userId: String
    ): Response<ResponseBody>

    // ── Batch Lead Creation ──
    @POST("api/leads/create")
    suspend fun createLeads(
        @Body request: BatchLeadRequest
    ): Response<BatchLeadResponse>

    // ── CRM Lead Listing ──
    @GET("api/leads")
    suspend fun getCrmLeads(): Response<List<CrmLeadEntry>>
}
