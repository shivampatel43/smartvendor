package com.example.smartvendor.data.model

data class Vendor(
    val vendorId: String = "",
    val name: String = "",
    val email: String = "",
    val location: String = "",
    val category: String = "",
    val websiteUrl: String = "",
    val imageUrl: String = "", // Added for Shop Image
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var distance: String = "", // Added for Nearby Sensing feature
    val rating: Double = 0.0,
    val numRatings: Int = 0
)
