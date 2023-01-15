package com.skycore.foodplace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.skycore.foodplace.adapter.ApiResponse
import com.skycore.foodplace.models.RestaurantsResponse
import com.skycore.foodplace.repository.RestaurantRepository

class MainViewModel(private val repository: RestaurantRepository) : ViewModel() {


    val restaurants: LiveData<ApiResponse<RestaurantsResponse>>
        get() = repository.restaurantList
}