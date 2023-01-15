package com.skycore.foodplace.apihelper

import com.skycore.foodplace.models.RestaurantsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap


interface ApiService {

    @GET("businesses/search")
    suspend fun getRestaurants(
        @QueryMap params: Map<String, String>,
    ): Response<RestaurantsResponse>
}