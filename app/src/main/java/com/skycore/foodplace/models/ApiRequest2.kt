package com.skycore.foodplace.models


data class ApiRequest2(
    val term: String? = "restaurants",
    val limit: String? = "15",
    val radius: String? = "",
    val sort_by: String? = "distance",
    val offset: String? = "0",
    val location: String? = "New York City",
    val longitude: String? = "",
    val latitude: String? = ""
) {
    fun toMap(): Map<String, String?> = mapOf(
        "term" to term,
        "limit" to limit,
        "radius" to radius,
        "sort_by" to sort_by,
        "offset" to offset,
        "location" to location,
        "longitude" to longitude,
        "latitude" to latitude,
    )

}