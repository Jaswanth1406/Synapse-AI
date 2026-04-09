package com.example.synapseai.data.api

import com.example.synapseai.BuildConfig

object ApiConstants {
    // Dograh Cloud
    val DOGRAH_BASE_URL: String = BuildConfig.DOGRAH_BASE_URL
    val DOGRAH_API_KEY: String = BuildConfig.DOGRAH_API_KEY
    val DOGRAH_AGENT_ID: String = BuildConfig.DOGRAH_AGENT_ID

    // FastAPI Backend
    val FASTAPI_BASE_URL: String = BuildConfig.FASTAPI_BASE_URL

    // ESPO CRM
    val ESPO_CRM_BASE_URL: String = BuildConfig.ESPO_CRM_BASE_URL
    val ESPO_CRM_API_KEY: String = BuildConfig.ESPO_CRM_API_KEY
}
