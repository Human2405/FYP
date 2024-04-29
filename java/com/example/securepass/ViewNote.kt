package com.example.securepass

import NoteAdapter
import PasswordAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lokile.encrypter.encrypters.imp.Encrypter

class ViewNote : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter

    //initialize firebase and set layout to view_note
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_note)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.notesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create an adapter with the empty password list
        adapter = NoteAdapter()

        // Set the adapter for the RecyclerView
        recyclerView.adapter = adapter

        // Fetch passwords from Firestore and update the adapter with the data
        fetchNotes()

        adapter.setOnDeleteClickListener { position ->
            val noteId = getNoteIdForPosition(position) // Get the password ID for the clicked item
            deleteNote(position, noteId)
        }

        adapter.setOnEditClickListener { position, noteId ->
            editNote(position, noteId)
        }

    }

    //start activity on EditNoteActivity
    private fun editNote(position: Int, noteId: String) {
        // Here, you can start the EditPasswordActivity and pass the passwordId to it.
        val intent = Intent(this, EditNoteActivity::class.java)
        intent.putExtra("noteId", noteId)
        intent.putExtra("adapterPosition", position)
        startActivity(intent)
    }

    // This function is used to initiate the deletion of a note from the adapter.
    private fun deleteNote(position: Int, noteId: String) {
        adapter.deleteNote(position, noteId)
    }

    // This function is used to retrieve the note ID for a given position from the adapter.
    private fun getNoteIdForPosition(position: Int): String {
        return adapter.getNoteIdForPosition(position)
    }

    //fetch all saved noted from firebase and decrypts it
    private fun fetchNotes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Use your app context and alias to create an Encrypter instance
            val appContext = applicationContext
            val encrypter = Encrypter.Builder(appContext, "note_alias")
                .build()

            // Create a Firestore reference to the "user_notes" sub-collection
            val userNotesCollection = FirebaseFirestore.getInstance()
                .collection("notes")
                .document(userId)
                .collection("user_notes")

            // Fetch notes from Firestore and update the adapter
            userNotesCollection.get().addOnSuccessListener { documents ->
                val noteList = mutableListOf<Notes>()
                for (document in documents) {
                    val noteId = document.id // Retrieve the document ID

                    //val noteTitle = document.getString("title") ?: ""

                    val encryptedNoteTitle = document.getString("title") ?: ""

                    val encryptedNoteContent = document.getString("content") ?: ""
                    Log.d("DecryptionTest", "Encrypted noteContent: $encryptedNoteContent")

                    try {
                        //Decrypt note title
                        val decryptedNoteTitle = encrypter.decryptOrNull(encryptedNoteTitle)

                        // Decrypt note content
                        val decryptedNoteContent = encrypter.decryptOrNull(encryptedNoteContent)
                        Log.d("DecryptionTest", "Decrypted noteContent: $decryptedNoteContent")

                        if (decryptedNoteContent != null && decryptedNoteTitle !=null) {
                            val noteItem = Notes(noteId, decryptedNoteTitle, decryptedNoteContent.toString())
                            noteList.add(noteItem)
                        } else {
                            Log.e("DecryptionTest", "Error decrypting note content")
                        }
                    } catch (e: Exception) {
                        Log.e("DecryptionTest", "Decryption error: ${e.message}")
                    }
                }

                // Update the adapter with the fetched data
                adapter.updateNotes(noteList)
            }.addOnFailureListener { exception ->
                // Handle Firestore query failure
                Log.e("Firestore", "Error querying Firestore: $exception")
                Toast.makeText(this, "Failed to retrieve note. Try later", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // This function is triggered on interaction to show or hide the content of an item in the RecyclerView.
    fun onShowNote(v: View) {
        val holder = recyclerView.findContainingViewHolder(v)

        if (holder != null && holder.adapterPosition != RecyclerView.NO_POSITION) {
            val position = holder.adapterPosition
            val note = adapter.getNoteAtPosition(position)

            Log.d("ViewNote", "isContentVisible before toggle: ${note.isContentVisible}")

            // Toggle the visibility of the note content
            note.isContentVisible = !note.isContentVisible

            Log.d("ViewNote", "isContentVisible after toggle: ${note.isContentVisible}")

            // Notify the adapter that the data has changed
            adapter.notifyItemChanged(position)
        }
    }


    //return to home screen
    fun backButton(v: View){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    //start new activity in SaveNoteActivity to save new note
    fun onNewNote(v : View){
        val intent = Intent(this, SaveNoteActivity::class.java)
        startActivity(intent)
    }
}
