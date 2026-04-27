package com.example.smartvendor.data.model

data class Order(
    val orderId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "Pending", // Pending, Accepted, Preparing, Out for Delivery, Delivered, Cancelled
    val timestamp: Long = System.currentTimeMillis()
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)
