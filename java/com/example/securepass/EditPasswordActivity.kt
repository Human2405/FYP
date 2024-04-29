package com.example.securepass

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

class EditPasswordActivity : AppCompatActivity() {

    //initialize adapter
    private lateinit var adapter: PasswordAdapter

    //set layout to edit_password and call fetch password details method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_password)

        adapter = PasswordAdapter()

        // Retrieve the password ID from the Intent
        val passwordId = intent.getStringExtra("passwordId")
        // Fetch the current password details and populate the EditText fields
        fetchCurrentPasswordDetails(passwordId)

        val saveButton = findViewById<Button>(R.id.editSaveButton)
        saveButton.setOnClickListener {
            // Save the updated password details
            saveUpdatedPasswordDetails(passwordId)
        }
    }

    //fetch saved password details to be edited
    private fun fetchCurrentPasswordDetails(passwordId: String?) {
        if (passwordId != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val documentReference = firestore
                    .collection("passwords")
                    .document(userId)
                    .collection("user_passwords")
                    .document(passwordId)

                // Use app context and alias to create an Encrypter instance
                val appContext = applicationContext
                val encrypter = Encrypter.Builder(appContext, "password_alias")
                    .build()

                documentReference.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val encryptedUsername = documentSnapshot.getString("username")
                            val encryptedPassword = documentSnapshot.getString("password")
                            val encryptedWebsite = documentSnapshot.getString("website")
                            val encryptedUrl = documentSnapshot.getString("url")

                            //Decrypt password details
                            val username = encryptedUsername?.let { encrypter.decryptOrNull(it) }
                            val password = encryptedPassword?.let { encrypter.decryptOrNull(it) }
                            val website = encryptedWebsite?.let { encrypter.decryptOrNull(it) }
                            val url = encryptedUrl?.let { encrypter.decryptOrNull(it) }


                            // Populate the EditText fields with the retrieved data
                            val usernameEditText = findViewById<EditText>(R.id.editUsernameEditText)
                            val passwordEditText = findViewById<EditText>(R.id.editPasswordEditText)
                            val websiteEditText = findViewById<EditText>(R.id.editWebsiteEditText)
                            val urlEditText = findViewById<EditText>(R.id.editUrlEditText)

                            usernameEditText.setText(username?.toString()?:"")
                            passwordEditText.setText(password?.toString()?:"")
                            websiteEditText.setText(website?.toString()?:"")
                            urlEditText.setText(url?.toString()?:"")
                        } else {
                            Toast.makeText(this, "Document doesn't exist", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Log.e("Firestore", "Error fetching password details: $e")
                    }
            }
        }
    }


    //save updated password detail to firestore after encryption
    private fun saveUpdatedPasswordDetails(passwordId: String?) {

        val appContext = applicationContext
        val encrypter = Encrypter.Builder(appContext, "password_alias")
            .build()

        if (passwordId != null) {
            // Retrieve the new data from the EditText fields.
            val updatedUsername = findViewById<EditText>(R.id.editUsernameEditText).text.toString()
            val updatedPassword = findViewById<EditText>(R.id.editPasswordEditText).text.toString()
            val updatedWebsite = findViewById<EditText>(R.id.editWebsiteEditText).text.toString()
            val updatedUrl = findViewById<EditText>(R.id.editUrlEditText).text.toString()

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val documentReference = firestore
                    .collection("passwords")
                    .document(userId)
                    .collection("user_passwords")
                    .document(passwordId)

                val encryptedUsername = encrypter.encryptOrNull(updatedUsername.toByteArray())
                val encryptedPassword = encrypter.encryptOrNull(updatedPassword.toByteArray())
                val encryptedWebsiteName = encrypter.encryptOrNull(updatedWebsite.toByteArray())
                val encryptedUrlName = encrypter.encryptOrNull(updatedUrl.toByteArray())

                if (encryptedUsername != null && encryptedPassword != null
                    && encryptedWebsiteName != null && encryptedUrlName != null
                ) {
                    // Convert byte arrays to Base64 strings
                    val base64Username = encryptedUsername.toStringData()
                    val base64Password = encryptedPassword.toStringData()
                    val base64WebsiteName = encryptedWebsiteName.toStringData()
                    val base64UrlName = encryptedUrlName.toStringData()

                    // Create a map to represent the password data
                    val updatedData = mapOf(
                        "username" to base64Username,
                        "password" to base64Password,
                        "website" to base64WebsiteName,
                        "url" to base64UrlName
                    )

                    documentReference.update(updatedData)
                        .addOnSuccessListener {
                            // Password details updated successfully

                            // Navigate back to the ViewPassword activity
                            val updatedPassword = Password(
                                passwordId,
                                updatedUsername,
                                updatedPassword,
                                updatedWebsite,
                                updatedUrl
                            )
                            //val adapterPosition = intent.getIntExtra("adapterPosition", -1)
                            adapter.updatePassword(updatedPassword)
                            Toast.makeText(this, "Password information updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ViewPassword::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            // Handle the error
                            Toast.makeText(this, "Failed to update password information", Toast.LENGTH_SHORT).show()
                            Log.e("Firestore", "Error updating password details: $e")
                        }
                }
            }
        }
    }

    //back button to return to ViewPassword screen
    fun editBackButton(v: View){
        val intent = Intent(this, ViewPassword::class.java)
        startActivity(intent)
    }
}