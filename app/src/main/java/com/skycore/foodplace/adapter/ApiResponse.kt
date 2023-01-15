package com.skycore.foodplace.adapter

sealed class ApiResponse<T>(val successData: T? = null, val errorData: String? = null) {
    class Loading<T> : ApiResponse<T>()
    class Success<T>(success: T? = null) : ApiResponse<T>(successData = success)
    class Error<T>(error: String) : ApiResponse<T>(errorData = error)
}