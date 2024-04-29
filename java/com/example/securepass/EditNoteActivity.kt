package com.example.securepass

import NoteAdapter
import PasswordAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lokile.encrypter.encrypters.imp.Encrypter

class EditNoteActivity : AppCompatActivity() {

    //initialize adapter
    private lateinit var adapter: NoteAdapter

    //set layout to edit_note and call fetch note details method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_note)

        adapter = NoteAdapter()
        // Retrieve the password ID from the Intent
        val noteId = intent.getStringExtra("noteId")
        // Fetch the current password details and populate the EditText fields
        fetchCurrentNoteDetails(noteId)

        val saveButton = findViewById<Button>(R.id.editNoteSaveButton)
        saveButton.setOnClickListener {
            // Save the updated password details
            saveUpdatedNoteDetails(noteId)
        }
    }

    //fetch saved details for the particular identity that needs to be edited
    private fun fetchCurrentNoteDetails(noteId: String?) {
        if (noteId != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val documentReference = firestore
                    .collection("notes")
                    .document(userId)
                    .collection("user_notes")
                    .document(noteId)

                val appContext = applicationContext
                val encrypter = Encrypter.Builder(appContext, "note_alias")
                    .build()

                documentReference.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val encryptedNoteTitle = documentSnapshot.getString("title")
                            val encryptedNoteContent = documentSnapshot.getString("content")

                            //Decrypt note details
                            val noteTitle = encryptedNoteTitle?.let { encrypter.decryptOrNull(it) }
                            val noteContent = encryptedNoteContent?.let { encrypter.decryptOrNull(it) }


                            // Populate the EditText fields with the retrieved data
                            val noteTitleEditText = findViewById<EditText>(R.id.editNoteTitleEditText)
                            val noteContentEditText = findViewById<EditText>(R.id.editNoteContentEditText)

                            noteTitleEditText.setText(noteTitle?.toString()?:"")
                            noteContentEditText.setText(noteContent?.toString()?:"")

                        } else {
                            // Handle the case where the document doesn't exist
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Log.e("Firestore", "Error fetching note details: $e")
                    }
            }
        }
    }

    //save edited note details to firestore after encryption
    private fun saveUpdatedNoteDetails(noteId: String?) {

        val appContext = applicationContext
        val encrypter = Encrypter.Builder(appContext, "note_alias")
            .build()

        if (noteId != null) {
            // Retrieve the new data from the EditText fields.
            val updatedNoteTitle =
                findViewById<EditText>(R.id.editNoteContentEditText).text.toString()
            val updatedNoteContent =
                findViewById<EditText>(R.id.editNoteTitleEditText).text.toString()


            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val documentReference = firestore
                    .collection("notes")
                    .document(userId)
                    .collection("user_notes")
                    .document(noteId)


                //encrypt updated note details
                val encryptedNoteTitle = encrypter.encryptOrNull(updatedNoteTitle.toByteArray())
                val encryptedNoteContent = encrypter.encryptOrNull(updatedNoteContent.toByteArray())


                if (encryptedNoteContent != null && encryptedNoteTitle != null) {
                    // Convert byte array to Base64 string
                    val base64NoteContent = encryptedNoteContent.toStringData()
                    val base64NoteTitle = encryptedNoteTitle.toStringData()


                    // Create a map to represent the note data
                    val updatedData = mapOf(
                        "title" to base64NoteTitle,
                        "content" to base64NoteContent  // Store the encrypted note content
                    )

                    documentReference.update(updatedData)
                        .addOnSuccessListener {
                            // Note details updated successfully

                            // Navigate back to the ViewNote activity
                            val updatedNote = Notes(noteId, updatedNoteContent, updatedNoteTitle)
                            adapter.updateNote(updatedNote)
                            Toast.makeText(this, "Note information updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ViewNote::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            // Handle the error
                            Toast.makeText(this, "Failed to update note information", Toast.LENGTH_SHORT).show()
                            Log.e("Firestore", "Error updating note details: $e")
                        }
                }
            }
        }
    }

    //back button to return to View Note screen
    fun noteBackButton(v: View){
        val intent = Intent(this, ViewNote::class.java)
        startActivity(intent)
    }
}