package com.example.zenith

data class Expense(
    var id: String? = null, // Firebase will use this for the key
    val amount: Double = 0.0,
    val date: String = "",
    val category: String = "",
    val description: String = "",
    val photoUrl: String? = null // Added field for photo URL
)
