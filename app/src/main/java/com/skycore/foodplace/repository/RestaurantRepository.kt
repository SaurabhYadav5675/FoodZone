package com.skycore.foodplace.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.skycore.foodplace.adapter.ApiResponse
import com.skycore.foodplace.apihelper.ApiService
import com.skycore.foodplace.models.RestaurantsResponse

class RestaurantRepository(private val apiService: ApiService) {

    private val restaurantLiveData = MutableLiveData<ApiResponse<RestaurantsResponse>>()

    val restaurantList: LiveData<ApiResponse<RestaurantsResponse>>
        get() = restaurantLiveData

    suspend fun getRestaurants(
        params: Map<String, String>
    ) {
        try {
            restaurantLiveData.postValue(ApiResponse.Loading())

            val result = apiService.getRestaurants(params)
            if (result.body() != null) {
                restaurantLiveData.postValue(ApiResponse.Success(result.body()))
            } else if (result.errorBody() != null) {
                restaurantLiveData.postValue(ApiResponse.Error("Api calling error"))
            }
        } catch (ex: Exception) {
            restaurantLiveData.postValue(ApiResponse.Error(ex.message.toString()))
        }

    }
}