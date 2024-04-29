package com.example.securepass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lokile.encrypter.encrypters.EncryptedData
import com.lokile.encrypter.encrypters.imp.Encrypter

class SaveNoteActivity : AppCompatActivity() {

    //set layout to save_note
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.save_note)
    }

    //retrieve user's input by converting it to string an save to firestore after encryption
    fun onSaveNote(v: View) {
        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        val notesEditText = findViewById<EditText>(R.id.notesEditText)

        val notesTitle = titleEditText.text.toString()
        val notesContent = notesEditText.text.toString()

        // Get the currently logged-in user's UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.e("Firestore", "user uid for save notes: $userId")

        if (userId != null) {
            // Use app context and alias to create an Encrypter instance
            val appContext = applicationContext
            val encrypter = Encrypter.Builder(appContext, "note_alias")
                .build()

            //Encrypt note title
            val encryptedNoteTitle: EncryptedData? = encrypter.encryptOrNull(notesTitle.toByteArray())

            // Encrypt note content before saving to Firestore
            val encryptedNoteContent: EncryptedData? = encrypter.encryptOrNull(notesContent.toByteArray())

            if (encryptedNoteContent != null && encryptedNoteTitle !=null) {
                // Convert byte array to Base64 string
                val base64NoteContent = encryptedNoteContent.toStringData()
                val base64NoteTitle = encryptedNoteTitle.toStringData()

                // Create a reference to the "notes" collection and the user's document
                val userDoc = FirebaseFirestore.getInstance().collection("notes").document(userId)

                // Create a map to represent the note data
                val noteData = mapOf(
                    "title" to base64NoteTitle,
                    "content" to base64NoteContent  // Store the encrypted note content
                )

                // Store multiple notes for each user in a sub-collection
                userDoc.collection("user_notes").add(noteData)
                    .addOnSuccessListener {
                        // Note data saved successfully
                        Log.e("Firestore", "Note data saved successfully")
                        Toast.makeText(this, "Note information saved successfully", Toast.LENGTH_SHORT).show()
                        val homeIntent = Intent(this, ViewNote::class.java)
                        startActivity(homeIntent)
                    }
                    .addOnFailureListener { e ->
                        // Handle note data save failure
                        Toast.makeText(this, "Failed to save note information", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error saving note data: $e")
                    }
            } else {
                Log.e("EncryptionTest", "Error encrypting note content")
            }
        } else {
            // Handle the case where the user is not logged in
            Log.e("Firestore", "User is not logged in")
        }
    }


    //return to ViewNote screen
    fun backButton(v: View){
        val intent = Intent(this, ViewNote::class.java)
        startActivity(intent)
    }
}