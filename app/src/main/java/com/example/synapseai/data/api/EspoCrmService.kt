package com.example.synapseai.data.api

import com.example.synapseai.data.model.EspoCallListResponse
import com.example.synapseai.data.model.EspoLeadListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface EspoCrmService {

    @GET("api/v1/Lead")
    suspend fun getLeads(
        @Query("maxSize") maxSize: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("orderBy") orderBy: String = "createdAt",
        @Query("order") order: String = "desc"
    ): Response<EspoLeadListResponse>

    @GET("api/v1/Call")
    suspend fun getCallLogs(
        @Query("maxSize") maxSize: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("orderBy") orderBy: String = "dateStart",
        @Query("order") order: String = "desc"
    ): Response<EspoCallListResponse>
}
