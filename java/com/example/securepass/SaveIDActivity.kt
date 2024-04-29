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

class SaveIDActivity : AppCompatActivity(){

    //set layout to save_identity_card
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.save_identity_card)
    }

    //retrieve user's input by converting it to string save to firestore after encryption
    fun onSaveID(v: View) {
        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val addressEditText = findViewById<EditText>(R.id.addressEditText)
        val idNumberEditText = findViewById<EditText>(R.id.identityNumberEditText)

        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val address = addressEditText.text.toString()
        val idNumber = idNumberEditText.text.toString()

        // Get the currently logged-in user's UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.e("Firestore", "user uid for save id: $userId")

        if (userId != null) {
            // Create a reference to the "identities" collection and the user's document
            val userIdentityDoc = FirebaseFirestore.getInstance().collection("identities").document(userId)

            // Use app context and alias to create an Encrypter instance
            val appContext = applicationContext
            val encrypter = Encrypter.Builder(appContext, "identity_alias")
                .build()

            // Encrypt identity data before saving to Firestore
            val encryptedFirstName: EncryptedData? = encrypter.encryptOrNull(firstName.toByteArray())
            val encryptedLastName: EncryptedData? = encrypter.encryptOrNull(lastName.toByteArray())
            val encryptedAddress: EncryptedData? = encrypter.encryptOrNull(address.toByteArray())
            val encryptedIdNumber: EncryptedData? = encrypter.encryptOrNull(idNumber.toByteArray())

            if (encryptedFirstName != null && encryptedLastName != null && encryptedAddress != null && encryptedIdNumber != null) {
                // Convert byte arrays to Base64 strings
                val base64FirstName = encryptedFirstName.toStringData()
                val base64LastName = encryptedLastName.toStringData()
                val base64Address = encryptedAddress.toStringData()
                val base64IdNumber = encryptedIdNumber.toStringData()

                // Create a map to represent the encrypted identity data
                val encryptedIdentityData = mapOf(
                    "firstName" to base64FirstName,
                    "lastName" to base64LastName,
                    "address" to base64Address,
                    "IDNumber" to base64IdNumber
                )

                // Store multiple identities for each user in a sub-collection
                userIdentityDoc.collection("user_identities").add(encryptedIdentityData)
                    .addOnSuccessListener {
                        // Identity data saved successfully
                        Log.e("Firestore", "Identity data saved successfully")
                        Toast.makeText(this, "Identity information saved successfully", Toast.LENGTH_SHORT).show()
                        val homeIntent = Intent(this, ViewIdentity::class.java)
                        startActivity(homeIntent)
                    }
                    .addOnFailureListener { e ->
                        // Handle identity data save failure
                        Toast.makeText(this, "Failed to save identity information", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error saving identity data: $e")
                    }
            } else {
                Log.e("EncryptionTest", "Error encrypting identity data")
            }
        } else {
            // Handle the case where the user is not logged in
            Log.e("Firestore", "User is not logged in")
        }
    }


    //return to ViewIdentity screen
    fun backButton(v: View){
        val intent = Intent(this, ViewIdentity::class.java)
        startActivity(intent)
    }
}