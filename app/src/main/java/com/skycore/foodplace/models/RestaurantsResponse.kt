package com.skycore.foodplace.models

data class RestaurantsResponse(
    val businesses: List<Businesse>,
    val region: Region,
    val total: Int
)