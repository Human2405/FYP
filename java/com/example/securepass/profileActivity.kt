package com.example.securepass

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class profileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        // Initialize Firebase Auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)

        userNameTextView = findViewById(R.id.userName)

        // Retrieve user's first name and last name from Firestore
        retrieveUserDetails()
    }

    private fun retrieveUserDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null){
            val userRef = firestore.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")

                        // Set user's name in the TextView
                        firstName?.let { fName ->
                            lastName?.let { lName ->
                                val fullName = "$fName $lName"
                                userNameTextView.text = fullName
                            }
                        }
                    } else {
                        // Handle document not exists
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle exceptions
                }
        }
    }

    fun onChangePassClick(v: View){
        val emailEditText = EditText(this)
        emailEditText.hint = "Enter your email"

        AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setMessage("Enter your email to reset the password")
            .setView(emailEditText)
            .setPositiveButton("Reset") { _, _ ->
                val email = emailEditText.text.toString()

                if (email.isNotEmpty()) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Password reset email sent to $email",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Log out the user
                                FirebaseAuth.getInstance().signOut()

                                // Navigate to the login activity
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to send password reset email. Please check your email and try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Enter your email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun backButton(v: View){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}