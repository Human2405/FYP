package com.example.securepass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardholder: String,
    val cardNumber: String,
    val cvc: String,
    val expiry: String,
    val pin: String
)

