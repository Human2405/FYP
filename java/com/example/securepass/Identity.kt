package com.example.securepass

// Data class representing a identity with essential properties
data class Identity(
    val identityId: String, //id of identity from firebase
    val firstName: String,
    val lastName: String,
    val address: String,
    val IDNumber: String,
    var isContentVisible: Boolean = false      //mask identity
)
