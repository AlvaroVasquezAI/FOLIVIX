package com.example.folivix.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.folivix.data.preferences.AppPreferences
import com.example.folivix.FolivixApplication

object RetrofitClient {
    private const val PORT = "5000"
    private const val PROTOCOL = "http://"

    private val appPreferences by lazy {
        AppPreferences(FolivixApplication.appContext)
    }

    private val serverIp: String
        get() = runBlocking {
            appPreferences.serverIp.first()
        }

    private val baseUrl: String
        get() = "$PROTOCOL$serverIp:$PORT/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val apiService: ApiService
        get() = retrofit.create(ApiService::class.java)
}