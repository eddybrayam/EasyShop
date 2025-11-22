package com.example.easyshop.model

// Este es el molde para guardar los pedidos en Firebase
data class OrderModel(
    val orderId: String = "",
    val userId: String = "",
    val products: List<ProductModel> = emptyList(),
    val totalPrice: Float = 0f,
    val address: String = "",
    val status: String = "En Camino",
    val date: Long = System.currentTimeMillis()
)