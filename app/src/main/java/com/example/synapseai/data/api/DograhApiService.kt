package com.example.synapseai.data.api

import com.example.synapseai.data.model.DograhCallRequest
import com.example.synapseai.data.model.DograhCallResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface DograhApiService {

    @POST("api/v1/public/agent/{agentId}")
    suspend fun triggerOutboundCall(
        @Path("agentId") agentId: String,
        @Body request: DograhCallRequest
    ): Response<DograhCallResponse>
}
