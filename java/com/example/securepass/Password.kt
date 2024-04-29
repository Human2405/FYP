package com.example.securepass

// Data class representing a Password with essential properties
data class Password(
    val passwordId: String, //id of password in firebase
    val websiteName: String,
    val username: String,
    val password: String,
    val url: String,
    var isContentVisible: Boolean = false   //mask password
)
