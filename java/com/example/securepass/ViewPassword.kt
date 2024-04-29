package com.example.securepass

import PasswordAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lokile.encrypter.encrypters.imp.Encrypter

class ViewPassword : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PasswordAdapter

    //initialize firebase and set layout to view_password
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_password)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.passwordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create an adapter with the empty password list
        adapter = PasswordAdapter()

        // Set the adapter for the RecyclerView
        recyclerView.adapter = adapter

        // Fetch passwords from Firestore and update the adapter with the data
        fetchPasswords()

        adapter.setOnDeleteClickListener { position ->
            val passwordId = getPasswordIdForPosition(position) // Get the password ID for the clicked item
            deletePassword(position, passwordId)
        }

        adapter.setOnEditClickListener { position, passwordId ->
            editPassword(position, passwordId)
        }

    }

    //start activity on EditPasswordActivity
    private fun editPassword(position: Int, passwordId: String) {
        // Here, you can start the EditPasswordActivity and pass the passwordId to it.
        val intent = Intent(this, EditPasswordActivity::class.java)
        intent.putExtra("passwordId", passwordId)
        intent.putExtra("adapterPosition", position)
        startActivity(intent)
    }

    // This function is used to initiate the deletion of a password from the adapter.
    private fun deletePassword(position: Int, passwordId: String) {
        adapter.deletePassword(position, passwordId)
    }

    // This function is used to retrieve the password ID for a given position from the adapter.
    private fun getPasswordIdForPosition(position: Int): String {
        return adapter.getPasswordIdForPosition(position)
    }

    //fetch all the saved password from firebase and decrypts it
    private fun fetchPasswords() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Create a Firestore reference to the "user_passwords" sub-collection
            val userPasswordsCollection = FirebaseFirestore.getInstance()
                .collection("passwords")
                .document(userId)
                .collection("user_passwords")

            // app context and alias to create an Encrypter instance
            val appContext = applicationContext
            val encrypter = Encrypter.Builder(appContext, "password_alias")
                .build()

            // Fetch passwords from Firestore and update the adapter
            userPasswordsCollection.get().addOnSuccessListener { documents ->
                val passwordList = mutableListOf<Password>()
                for (document in documents) {
                    val passwordId = document.id // Retrieve the document ID

                    val username = document.getString("username")?.let { encryptedData ->
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error for username: ${e.message}")
                            ""
                        }
                    } ?: ""

                    val password = document.getString("password")?.let { encryptedData ->
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error for password: ${e.message}")
                            ""
                        }
                    } ?: ""

                    val website = document.getString("website")?.let { encryptedData ->
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error for website: ${e.message}")
                            ""
                        }
                    } ?: ""

                    val url = document.getString("url")?.let { encryptedData ->
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error for url: ${e.message}")
                            ""
                        }
                    } ?: ""

                    /*val test: String? = document.getString("username2")
                    val key: String? = document.getString("key")

                    if (test != null && key != null) {
                        val decryptedText = decrypt(test, key)
                        Log.e("Decrypted","Decrypted Text: $decryptedText")
                    } else {
                        println("Error: Either 'username2' or 'key' is null.")
                    }*/

                    val passwordItem = Password(passwordId, website, username, password, url)
                    passwordList.add(passwordItem)
                }

                // Update the adapter with the fetched data
                adapter.updatePasswords(passwordList)
            }.addOnFailureListener { exception ->
                // Handle Firestore query failure
                Log.e("Firestore", "Error querying Firestore: $exception")
                Toast.makeText(this, "Failed to retrieve passwords. Try later", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // This function is triggered on interaction to show or hide the content of an item in the RecyclerView.
    fun onShowPass(v: View) {
        val holder = recyclerView.findContainingViewHolder(v)

        if (holder != null && holder.adapterPosition != RecyclerView.NO_POSITION) {
            val position = holder.adapterPosition
            val pass = adapter.getPassAtPosition(position)

            Log.d("ViewNote", "isContentVisible before toggle: ${pass.isContentVisible}")

            // Toggle the visibility of the note content
            pass.isContentVisible = !pass.isContentVisible

            Log.d("ViewNote", "isContentVisible after toggle: ${pass.isContentVisible}")

            // Notify the adapter that the data has changed
            adapter.notifyItemChanged(position)
        }
    }

    //return to home screen
    fun backButton(v: View){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    //start activity on SavePasswordActivity to save a new password
    fun onNewPass(v : View){
        val intent = Intent(this, SavePasswordActivity::class.java)
        startActivity(intent)
    }
}
