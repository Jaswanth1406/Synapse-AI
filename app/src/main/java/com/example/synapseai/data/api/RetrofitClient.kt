package com.example.synapseai.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ── Dograh Cloud Client ──
    private val dograhClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-API-Key", ApiConstants.DOGRAH_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            })
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val dograhApi: DograhApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.DOGRAH_BASE_URL.trimEnd('/') + "/")
            .client(dograhClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DograhApiService::class.java)
    }

    // ── FastAPI Backend Client ──
    private val fastApiClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val fastApi: FastApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.FASTAPI_BASE_URL.trimEnd('/') + "/")
            .client(fastApiClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FastApiService::class.java)
    }

    // ── ESPO CRM Client ──
    private val espoCrmClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Api-Key", ApiConstants.ESPO_CRM_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            })
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val espoCrmApi: EspoCrmService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.ESPO_CRM_BASE_URL.trimEnd('/') + "/")
            .client(espoCrmClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EspoCrmService::class.java)
    }
}
