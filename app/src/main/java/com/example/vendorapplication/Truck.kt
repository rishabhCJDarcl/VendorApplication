package com.example.vendorapplication

data class Truck(
    val truckNumber: String,
    val from: String,
    val to: String,
    val type: String,
    val passing: String,
    val capacity: String = "15 Ton",
    val etaHours: Int = 36,
    val isAvailable: Boolean = true
)
