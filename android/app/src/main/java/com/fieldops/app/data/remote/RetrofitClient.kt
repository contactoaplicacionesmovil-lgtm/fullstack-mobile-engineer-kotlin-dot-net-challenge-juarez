package com.fieldops.app.data.remote

import com.fieldops.app.Session
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        val baseUrl = if (Session.BASE_URL.endsWith("/")) {
            Session.BASE_URL
        } else {
            "${Session.BASE_URL}/"
        }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val workOrdersApiService: WorkOrdersApiService by lazy {
        retrofit.create(WorkOrdersApiService::class.java)
    }

    private const val TIMEOUT_SECONDS = 10L
}
