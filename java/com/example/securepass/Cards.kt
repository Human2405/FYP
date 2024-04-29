package com.example.securepass

// Data class representing a Card with essential properties
data class Cards(
    val cardholderName: String,
    val cardNumber: String,
    val CVC: String,
    val cardExpiry: String,
    val cardPIN: String,
    val cardId: String, //id of card in firebase
    var isContentVisible: Boolean = false      // mask card
)