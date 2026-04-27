package com.example.smartvendor.data.model

data class Product(
    val productId: String = "",
    val vendorId: String = "",
    val name: String = "",
    val category: String = "",
    val code: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = ""
)
