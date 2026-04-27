package com.example.smartvendor.data.model

data class Review(
    val reviewId: String = "",
    val vendorId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
