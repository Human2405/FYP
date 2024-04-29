package com.example.securepass

import IdentityAdapter
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

class EditIdentityActivity : AppCompatActivity() {

    //initialize adapter
    private lateinit var adapter: IdentityAdapter

    //set layout to edit_identity and call fetch identity details method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_identity)

        adapter = IdentityAdapter()
        // Retrieve the password ID from the Intent
        val identityId = intent.getStringExtra("identityId")
        // Fetch the current password details and populate the EditText fields
        fetchCurrentIdentityDetails(identityId)

        val saveButton = findViewById<Button>(R.id.editIdentitySaveButton)
        saveButton.setOnClickListener {
            // Save the updated password details
            saveUpdatedIdentityDetails(identityId)
        }
    }

    //fetch saved details for the particular identity that needs to be edited
    private fun fetchCurrentIdentityDetails(identityId: String?) {
        if (identityId != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val documentReference = firestore
                    .collection("identities")
                    .document(userId)
                    .collection("user_identities")
                    .document(identityId)

                //get alias for Keystore
                val appContext = applicationContext
                val encrypter = Encrypter.Builder(appContext, "identity_alias")
                    .build()

                documentReference.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val encryptedFirstName = documentSnapshot.getString("firstName")
                            val encryptedLastName = documentSnapshot.getString("lastName")
                            val encryptedAddress = documentSnapshot.getString("address")
                            val encryptedIDNumber = documentSnapshot.getString("IDNumber")


                            //Decrypt ID details
                            val firstName = encryptedFirstName?.let { encrypter.decryptOrNull(it) }
                            val lastName = encryptedLastName?.let { encrypter.decryptOrNull(it) }
                            val address = encryptedAddress?.let { encrypter.decryptOrNull(it) }
                            val IDNumber = encryptedIDNumber?.let { encrypter.decryptOrNull(it) }

                            // Populate the EditText fields with the retrieved data
                            val firstNameEditText = findViewById<EditText>(R.id.editFirstNameEditText)
                            val lastNameEditText = findViewById<EditText>(R.id.editLastNameEditText)
                            val addressEditText = findViewById<EditText>(R.id.editAddressEditText)
                            val IDNumberEditText = findViewById<EditText>(R.id.editIDNumberEditText)

                            firstNameEditText.setText(firstName?.toString()?:"")
                            lastNameEditText.setText(lastName?.toString()?:"")
                            addressEditText.setText(address?.toString()?:"")
                            IDNumberEditText.setText(IDNumber?.toString()?:"")
                        } else {
                            Toast.makeText(this, "Document doesn't exist", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Log.e("Firestore", "Error fetching identity details: $e")
                    }
            }
        }
    }


    //save the edited identity details to firestore after encrypting it
    private fun saveUpdatedIdentityDetails(identityId: String?) {

        val appContext = applicationContext
        val encrypter = Encrypter.Builder(appContext, "identity_alias")
            .build()

        if (identityId != null) {
            // Retrieve the new data from the EditText fields.
            val updatedFirstName = findViewById<EditText>(R.id.editFirstNameEditText).text.toString()
            val updatedLastName = findViewById<EditText>(R.id.editLastNameEditText).text.toString()
            val updatedAddress = findViewById<EditText>(R.id.editAddressEditText).text.toString()
            val updatedIDNumber = findViewById<EditText>(R.id.editIDNumberEditText).text.toString()

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val documentReference = firestore
                    .collection("identities")
                    .document(userId)
                    .collection("user_identities")
                    .document(identityId)


                //encrypt updated identity
                val encryptedFirstName = encrypter.encryptOrNull(updatedFirstName.toByteArray())
                val encryptedLastName = encrypter.encryptOrNull(updatedLastName.toByteArray())
                val encryptedAddress = encrypter.encryptOrNull(updatedAddress.toByteArray())
                val encryptedIdNumber = encrypter.encryptOrNull(updatedIDNumber.toByteArray())

                if (encryptedFirstName != null && encryptedLastName != null && encryptedAddress != null && encryptedIdNumber != null) {
                    // Convert byte arrays to Base64 strings
                    val base64FirstName = encryptedFirstName.toStringData()
                    val base64LastName = encryptedLastName.toStringData()
                    val base64Address = encryptedAddress.toStringData()
                    val base64IdNumber = encryptedIdNumber.toStringData()

                    // Create a map to represent the encrypted identity data
                    val updatedData = mapOf(
                        "firstName" to base64FirstName,
                        "lastName" to base64LastName,
                        "address" to base64Address,
                        "IDNumber" to base64IdNumber
                    )
                    documentReference.update(updatedData)
                        .addOnSuccessListener {
                            // Password details updated successfully

                            // Navigate back to the ViewPassword activity
                            val updatedIdentity = Identity(
                                identityId,
                                updatedFirstName,
                                updatedLastName,
                                updatedAddress,
                                updatedIDNumber
                            )
                            adapter.updateIdentity(updatedIdentity)
                            Toast.makeText(this, "Identity information updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ViewIdentity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            // Handle the error
                            Toast.makeText(this, "Failed to update identity information", Toast.LENGTH_SHORT).show()
                            Log.e("Firestore", "Error updating identity details: $e")
                        }
                }
            }
        }
    }

    //back button to return to ViewIdentity screen
    fun editBackButton(v: View){
        val intent = Intent(this, ViewIdentity::class.java)
        startActivity(intent)
    }
}