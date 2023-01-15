package com.skycore.foodplace.apihelper

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitHelper {
    private const val baseUrl = "https://api.yelp.com/v3/"
    private const val key =
        "XPFgzKwZGK1yqRxHi0d5xsARFOLpXIvccQj5jekqTnysweGyoIfVUHcH2tPfGq5Oc9kwKHPkcOjk2d1Xobn7aTjOFeop8x41IUfVvg2Y27KiINjYPADcE7Qza0RkX3Yx";

    private var apiService: ApiService? = null

    private var httpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest: Request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $key")
            .build()
        chain.proceed(newRequest)
    }.build()

    private fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    fun getApiService(): ApiService {
        if (apiService == null) {
            apiService = getInstance().create(ApiService::class.java)
        }
        return apiService as ApiService
    }

}