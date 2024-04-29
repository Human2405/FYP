package com.example.securepass

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

class SignUp : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var verificationId: String? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var token: String? = null

    //initialize Firebase and set layout to sign_up
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        /*FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            token = task.result

            // Log and toast
            println(token)
            Log.d("hi", "myID: " + token)
            Toast.makeText(baseContext, "token is: " + token, Toast.LENGTH_SHORT).show()
        })*/
    }

    //retrieve user's input by converting it to string and save their login credentials to firestore (if password and confirm password match)
   /* fun onSignUpUser(v: View) {
        val usernameEditText = findViewById<EditText>(R.id.userNameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)

        val email = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val confirmPass = confirmPasswordEditText.text.toString()

        // Check if the password and confirm password match
        if (password == confirmPass) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // User signed up successfully
                        val user = auth.currentUser
                        if (user != null) {
                            val userId = user.uid // Unique user ID
                            val userDoc = db.collection("users").document(userId)

                            // Set user data in Firestore document
                            userDoc.set(
                                mapOf(
                                    "email" to email,
                                    "password" to password,
                                    "firstName" to firstName,
                                    "lastName" to lastName
                                )
                            ).addOnSuccessListener {
                                // User data saved in Firestore
                                Log.d("Firestore", "User data saved in Firestore")
                                Toast.makeText(this, "Sign-up was successful", Toast.LENGTH_SHORT).show()
                                val homeIntent = Intent(this, MainActivity::class.java)
                                startActivity(homeIntent)
                            }.addOnFailureListener { e ->
                                Log.e("Firestore", "Error saving user data: $e")
                            }
                        } else {
                            Log.e("Firestore", "User is null")
                        }
                    } else {
                        Log.e("Firestore", "Sign-up failed: ${task.exception}")
                    }
                }
        } else {
            // Password and confirm password do not match, show an error message
            Toast.makeText(this, "Password entered do not match", Toast.LENGTH_SHORT).show()
        }
    }*/


    //WORKING SIGN-UP
    // Retrieve user's input by converting it to string and save their login credentials to Firestore (if password and confirm password match)
    /*fun onSignUpUser(v: View) {
        val usernameEditText = findViewById<EditText>(R.id.userNameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)

        val email = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val confirmPass = confirmPasswordEditText.text.toString()

        // Check if any of the required fields are empty
        if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && confirmPass.isNotEmpty()) {
            // Check if the password and confirm password match
            if (password == confirmPass) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // User signed up successfully
                            val user = auth.currentUser
                            if (user != null) {
                                val userId = user.uid // Unique user ID
                                val userDoc = db.collection("users").document(userId)

                                // Set user data in Firestore document
                                userDoc.set(
                                    mapOf(
                                        "email" to email,
                                        "password" to password,
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                       //"token" to token
                                    )
                                ).addOnSuccessListener {
                                    // User data saved in Firestore
                                    Log.d("Firestore", "User data saved in Firestore")
                                    Toast.makeText(this, "Sign-up was successful", Toast.LENGTH_SHORT).show()
                                    val homeIntent = Intent(this, MainActivity::class.java)
                                    startActivity(homeIntent)
                                }.addOnFailureListener { e ->
                                    Log.e("Firestore", "Error saving user data: $e")
                                    Toast.makeText(this, "Sign-up was unsuccessful", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.e("Firestore", "User is null")
                            }
                        } else {
                            Log.e("Firestore", "Sign-up failed: ${task.exception}")
                        }
                    }
            } else {
                // Password and confirm password do not match, show an error message
                Toast.makeText(this, "Password entered do not match", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Some fields are empty, show an error message
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
        }
    }*/

    fun onSignUpUser(v: View) {
        val usernameEditText = findViewById<EditText>(R.id.userNameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)

        val email = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val confirmPass = confirmPasswordEditText.text.toString()

        // Check if any of the required fields are empty
        if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && confirmPass.isNotEmpty()) {
            // Validate the email format
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // Check if the password and confirm password match
                if (password == confirmPass) {
                    // Check if the password meets the minimum length of 7 characters
                    if (password.length >= 7) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // User signed up successfully
                                    val user = auth.currentUser
                                    if (user != null) {
                                        val userId = user.uid // Unique user ID
                                        val userDoc = db.collection("users").document(userId)

                                        // Set user data in Firestore document
                                        userDoc.set(
                                            mapOf(
                                                "email" to email,
                                                "password" to password,
                                                "firstName" to firstName,
                                                "lastName" to lastName
                                                //"token" to token
                                            )
                                        ).addOnSuccessListener {
                                            // User data saved in Firestore
                                            Log.d("Firestore", "User data saved in Firestore")
                                            Toast.makeText(this, "Sign-up was successful", Toast.LENGTH_SHORT).show()
                                            val homeIntent = Intent(this, MainActivity::class.java)
                                            startActivity(homeIntent)
                                        }.addOnFailureListener { e ->
                                            Log.e("Firestore", "Error saving user data: $e")
                                            Toast.makeText(this, "Sign-up was unsuccessful", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Log.e("Firestore", "User is null")
                                    }
                                } else {
                                    Log.e("Firestore", "Sign-up failed: ${task.exception}")
                                }
                            }
                    } else {
                        // Password does not meet the minimum length requirement
                        Toast.makeText(this, "Password must be at least 7 characters long", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Password and confirm password do not match, show an error message
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Email format is invalid
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Some fields are empty, show an error message
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
        }
    }



    /* fun onSignUpUser(v: View) {
         val usernameEditText = findViewById<EditText>(R.id.userNameEditText)
         val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
         val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
         val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
         val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
         val phoneNumberEditText = findViewById<EditText>(R.id.phoneNumberEditText)

         val email = usernameEditText.text.toString()
         val password = passwordEditText.text.toString()
         val firstName = firstNameEditText.text.toString()
         val lastName = lastNameEditText.text.toString()
         val confirmPass = confirmPasswordEditText.text.toString()
         val phoneNumber = phoneNumberEditText.text.toString()

         // Check if any of the required fields are empty
         if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && confirmPass.isNotEmpty() && phoneNumber.isNotEmpty()) {
             // Check if the password and confirm password match
             if (password == confirmPass) {
                 auth.createUserWithEmailAndPassword(email, password)
                     .addOnCompleteListener(this) { task ->
                         if (task.isSuccessful) {
                             // User signed up successfully
                             val user = auth.currentUser
                             if (user != null) {
                                 val userId = user.uid // Unique user ID
                                 val userDoc = db.collection("users").document(userId)

                                 // Set user data in Firestore document
                                 userDoc.set(
                                     mapOf(
                                         "email" to email,
                                         "password" to password,
                                         "firstName" to firstName,
                                         "lastName" to lastName,
                                         "phoneNumber" to phoneNumber
                                         //"token" to token
                                     )
                                 ).addOnSuccessListener {
                                     // User data saved in Firestore
                                     Log.d("Firestore", "User data saved in Firestore")
                                     Toast.makeText(this, "Sign-up was successful", Toast.LENGTH_SHORT).show()
                                     val homeIntent = Intent(this, MainActivity::class.java)
                                     startActivity(homeIntent)
                                 }.addOnFailureListener { e ->
                                     Log.e("Firestore", "Error saving user data: $e")
                                 }
                             } else {
                                 Log.e("Firestore", "User is null")
                             }
                         } else {
                             Log.e("Firestore", "Sign-up failed: ${task.exception}")
                         }
                     }
             } else {
                 // Password and confirm password do not match, show an error message
                 Toast.makeText(this, "Password entered do not match", Toast.LENGTH_SHORT).show()
             }
         } else {
             // Some fields are empty, show an error message
             Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
         }
     }*/

    /*fun onSignUpUser(v: View) {
        val usernameEditText = findViewById<EditText>(R.id.userNameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val phoneNumberEditText = findViewById<EditText>(R.id.phoneNumberEditText)

        val email = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val confirmPass = confirmPasswordEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()

        // Check if any of the required fields are empty
        if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && confirmPass.isNotEmpty() && phoneNumber.isNotEmpty()) {
            // Check if the password and confirm password match
            if (password == confirmPass) {
                // Create user with email and password
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // User signed up successfully
                            val user = auth.currentUser
                            if (user != null) {
                                val userId = user.uid // Unique user ID
                                val userDoc = db.collection("users").document(userId)

                                // Set user data in Firestore document
                                userDoc.set(
                                    mapOf(
                                        "email" to email,
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "phoneNumber" to phoneNumber
                                    )
                                ).addOnSuccessListener {
                                    // User data saved in Firestore
                                    Log.d("Firestore", "User data saved in Firestore")
                                    // Proceed with multi-factor authentication
                                    initiateMultiFactorAuthentication(phoneNumber)
                                }.addOnFailureListener { e ->
                                    Log.e("Firestore", "Error saving user data: $e")
                                }
                            } else {
                                Log.e("Firestore", "User is null")
                            }
                        } else {
                            Log.e("Firestore", "Sign-up failed: ${task.exception}")
                        }
                    }
            } else {
                // Password and confirm password do not match, show an error message
                Toast.makeText(this, "Password entered do not match", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Some fields are empty, show an error message
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initiateMultiFactorAuthentication(phoneNumber: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Verification Code")

        val input = EditText(this@SignUp)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val verificationCode = input.text.toString()
            if (verificationCode.isNotEmpty()) {
                verifyPhoneNumberWithCode(phoneNumber, verificationCode)
            } else {
                Toast.makeText(this@SignUp, "Verification code cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        builder.show()

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked when the verification is completed automatically
                // For now, you can ignore this callback as the SMS verification code is being entered manually
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked when an error occurs during verification
                Log.e("MultiFactorAuth", "Verification failed: $e")
                // Show an error message to the user
                Toast.makeText(this@SignUp, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // This callback is invoked when the verification code is successfully sent
                // Save the verification ID and token for later use
                // Note: You may choose to save the verification ID and token here for later use
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(phoneNumber: String, verificationCode: String) {
        val credential = verificationId?.let { PhoneAuthProvider.getCredential(it, verificationCode) }
        FirebaseAuth.getInstance().currentUser?.multiFactor?.getSession()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val multiFactorSession = task.result
                    val multiFactorAssertion = credential?.let {
                        PhoneMultiFactorGenerator.getAssertion(
                            it
                        )
                    }
                    if (multiFactorAssertion != null) {
                        FirebaseAuth.getInstance().currentUser?.multiFactor?.enroll(
                            multiFactorAssertion,
                            "My personal phone number"
                        )?.addOnCompleteListener { enrollTask ->
                            if (enrollTask.isSuccessful) {
                                // Multi-factor authentication enrollment completed
                                Toast.makeText(
                                    this@SignUp,
                                    "Multi-factor authentication enrollment completed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Failed to enroll multi-factor authentication
                                Log.e("MultiFactorAuth", "Failed to enroll multi-factor authentication", enrollTask.exception)
                                Toast.makeText(
                                    this@SignUp,
                                    "Failed to enroll multi-factor authentication. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    // Failed to get multi-factor session
                    Log.e("MultiFactorAuth", "Failed to get multi-factor session", task.exception)
                    Toast.makeText(
                        this@SignUp,
                        "Failed to get multi-factor session. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }*/


    //return to login (MainActivity) screen
    fun backButton(v: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}