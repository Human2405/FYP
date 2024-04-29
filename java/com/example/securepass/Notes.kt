package com.example.securepass

// Data class representing a Note with essential properties
data class Notes(
    val noteId: String, //id of note in firebase
    val noteTitle: String,
    val noteContent: String,
    var isContentVisible: Boolean = false   //mask note
)
