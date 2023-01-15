package com.skycore.foodplace.models

data class Location(
    val address1: String,
    val address2: String,
    val address3: String,
    val city: String,
    val country: String,
    val display_address: List<String>,
    val state: String,
    val zip_code: String
)