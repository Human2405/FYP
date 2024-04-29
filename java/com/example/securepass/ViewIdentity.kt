package com.example.securepass

import IdentityAdapter
import PasswordAdapter
import android.annotation.SuppressLint
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

class ViewIdentity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IdentityAdapter

    //initialize firebase and set layout to view_identity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_identity)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.identityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create an adapter with the empty password list
        adapter = IdentityAdapter()

        // Set the adapter for the RecyclerView
        recyclerView.adapter = adapter

        // Fetch passwords from Firestore and update the adapter with the data
        fetchIdentities()

        adapter.setOnDeleteClickListener { position ->
            val identityId = getIdentityIdForPosition(position) // Get the password ID for the clicked item
            deleteIdentity(position, identityId)
        }

        adapter.setOnEditClickListener { position, identityId ->
            editIdentity(position, identityId)
        }

    }

    //start activity on EditIdentityActivity
    private fun editIdentity(position: Int, identityId: String) {
        // Here, you can start the EditPasswordActivity and pass the passwordId to it.
        val intent = Intent(this, EditIdentityActivity::class.java)
        intent.putExtra("identityId", identityId)
        intent.putExtra("adapterPosition", position)
        startActivity(intent)
    }

    // This function is used to initiate the deletion of a identity from the adapter.
    private fun deleteIdentity(position: Int, identityId: String) {
        adapter.deleteIdentity(position, identityId)
    }

    // This function is used to retrieve the identity ID for a given position from the adapter.
    private fun getIdentityIdForPosition(position: Int): String {
        return adapter.getIdentityIdForPosition(position)
    }

    //fetch saved identities from firebase and decrypts it
    private fun fetchIdentities() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Create a Firestore reference to the "user_identities" sub-collection
            val userIdentitiesCollection = FirebaseFirestore.getInstance()
                .collection("identities")
                .document(userId)
                .collection("user_identities")

            // Use your app context and alias to create an Encrypter instance
            val appContext = applicationContext
            val encrypter = Encrypter.Builder(appContext, "identity_alias")
                .build()

            // Fetch identities from Firestore and update the adapter
            userIdentitiesCollection.get().addOnSuccessListener { documents ->
                val identityList = mutableListOf<Identity>()
                for (document in documents) {
                    val identityId = document.id // Retrieve the document ID

                    val firstName = document.getString("firstName")?.let { encryptedData ->
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error: ${e.message}")
                            ""
                        }
                    } ?: ""

                    val lastName = document.getString("lastName")?.let { encryptedData ->
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error: ${e.message}")
                            ""
                        }
                    } ?: ""

                    val address = document.getString("address")?.let { encryptedData ->
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error: ${e.message}")
                            ""
                        }
                    } ?: ""

                    val IDNumber = document.getString("IDNumber")?.let { encryptedData ->
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error: ${e.message}")
                            ""
                        }
                    } ?: ""

                    val identityItem = Identity(identityId, firstName, lastName, address, IDNumber)
                    identityList.add(identityItem)
                }

                // Update the adapter with the fetched data
                adapter.updateIdentities(identityList)
            }.addOnFailureListener { exception ->
                // Handle Firestore query failure
                Log.e("Firestore", "Error querying Firestore: $exception")
                Toast.makeText(this, "Failed to retrieve identity. Try later", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // This function is triggered on interaction to show or hide the content of an item in the RecyclerView.
    fun onShowIdentity(v: View) {
        val holder = recyclerView.findContainingViewHolder(v)

        if (holder != null && holder.adapterPosition != RecyclerView.NO_POSITION) {
            val position = holder.adapterPosition
            val note = adapter.getIDAtPosition(position)

            Log.d("ViewNote", "isContentVisible before toggle: ${note.isContentVisible}")

            // Toggle the visibility of the note content
            note.isContentVisible = !note.isContentVisible

            Log.d("ViewNote", "isContentVisible after toggle: ${note.isContentVisible}")

            // Notify the adapter that the data has changed
            adapter.notifyItemChanged(position)
        }
    }

    //return to Home screen
    fun backButton(v: View){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    //start new activity in SaveIDActivity to save a new identity
    fun onNewIdentity(v : View){
        val intent = Intent(this, SaveIDActivity::class.java)
        startActivity(intent)
    }
}
