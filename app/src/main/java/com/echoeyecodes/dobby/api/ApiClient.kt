package com.echoeyecodes.dobby.api

import android.content.Context
import com.echoeyecodes.dobby.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    private lateinit var retrofitBuilder: Retrofit.Builder
    private val BASE_URL = "https://dobbydownloader.picashot.co/"
    private lateinit var retrofit: Retrofit

    init {
        init()
    }

    companion object {
        @Volatile
        private var INSTANCE: ApiClient? = null
        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            val instance = ApiClient().apply {
            }
            INSTANCE = instance
            return instance
        }
    }

    private fun init() {
        val gson = GsonBuilder().setLenient().create()
        val httpclient = OkHttpClient.Builder()
        httpclient.addInterceptor { chain: Interceptor.Chain ->
            val originalRequest = chain.request()
            val version = BuildConfig.VERSION_NAME
            val versioncode = BuildConfig.VERSION_CODE.toString()
            val request = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("version", version).header("versioncode", versioncode).build()
            val response = chain.proceed(request)
            response
        }
        retrofitBuilder = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)).client(httpclient.build())
        retrofit = retrofitBuilder.build()
    }

    fun <T> getClient(t: Class<T>?): T {
        return retrofit.create(t)
    }
}