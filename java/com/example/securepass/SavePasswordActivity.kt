package com.example.securepass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lokile.encrypter.encrypters.EncryptedData
import com.lokile.encrypter.encrypters.imp.Encrypter
import java.util.concurrent.ThreadLocalRandom


class SavePasswordActivity : AppCompatActivity() {

    //set layout to save_password
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.save_password)
    }

    fun encryptPass(text: String, key: Int): String {
        return text.map {
            when {
                it.isLetter() -> {
                    val base = if (it.isUpperCase()) 'A' else 'a'
                    (((it.toInt() - base.toInt() + key) % 26 + 26) % 26 + base.toInt()).toChar()
                }
                it.isDigit() -> {
                    (((it.toInt() - '0'.toInt() + key) % 10 + 10) % 10 + '0'.toInt()).toChar()
                }
                else -> it
            }
        }.joinToString("")
    }

    //retrieve user's input by converting it to string and  save to firestore after encryption
    fun onSavePassword(v: View) {
        val websiteEditText = findViewById<EditText>(R.id.websiteEditText)
        val usernameEditText = findViewById<EditText>(R.id.usernamePassEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordPassEditText)
        val urlEditText = findViewById<EditText>(R.id.urlPassEditText)

        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val websiteName = websiteEditText.text.toString()
        val urlName = urlEditText.text.toString()

        //website encryption
        val key = (1..25).random()

        val webUsername = username
        val webPassword = password
        val webName = websiteName

        val encodedUser = encryptPass(webUsername, key)
        val encodedPass = encryptPass(webPassword, key)
        val encodedWeb = encryptPass(webName, key)


        // Get the currently logged-in user's UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.e("Firestore", "user uid for save password: $userId")

        if (userId != null) {
            // Create a reference to the "passwords" collection and the user's document
            val userDoc = FirebaseFirestore.getInstance().collection("passwords").document(userId)

            // Use app context and alias to create an Encrypter instance
            val appContext = applicationContext
            val encrypter = Encrypter.Builder(appContext, "password_alias")
                .build()

            Log.e("Firestore", "encryter for encryption: $encrypter")

            // Encrypt all fields before saving to Firestore
            val encryptedUsername: EncryptedData? = encrypter.encryptOrNull(username.toByteArray())
            val encryptedPassword: EncryptedData? = encrypter.encryptOrNull(password.toByteArray())
            val encryptedWebsiteName: EncryptedData? = encrypter.encryptOrNull(websiteName.toByteArray())
            val encryptedUrlName: EncryptedData? = encrypter.encryptOrNull(urlName.toByteArray())

            if (encryptedUsername != null && encryptedPassword != null
                && encryptedWebsiteName != null && encryptedUrlName != null
            ) {
                // Convert byte arrays to Base64 strings
                val base64Username = encryptedUsername.toStringData()
                val base64Password = encryptedPassword.toStringData()
                val base64WebsiteName = encryptedWebsiteName.toStringData()
                val base64UrlName = encryptedUrlName.toStringData()

                // Create a map to represent the password data
                val passwordData = mapOf(
                    "username" to base64Username,
                    "password" to base64Password,
                    "website" to base64WebsiteName,
                    "url" to base64UrlName,
                    "webUser" to encodedUser,
                    "webPass" to encodedPass,
                    "webName" to encodedWeb,
                    "web" to key
                )

                // Store multiple passwords for each user in a sub-collection
                userDoc.collection("user_passwords").add(passwordData)
                    .addOnSuccessListener {
                        // Password data saved successfully
                        Log.e("Firestore", "Password data saved successfully")
                        Toast.makeText(this, "Password information saved successfully", Toast.LENGTH_SHORT).show()
                        val homeIntent = Intent(this, ViewPassword::class.java)
                        startActivity(homeIntent)
                    }
                    .addOnFailureListener { e ->
                        // Handle password data save failure
                        Toast.makeText(this, "Failed to save password information", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error saving password data: $e")
                    }
            } else {
                Log.e("EncryptionTest", "Error encrypting password data")
            }
        } else {
            // Handle the case where the user is not logged in
            Log.e("Firestore", "User is not logged in")
        }
    }




//return to ViewPassword screen
    fun backButton(v:View){
        val intent = Intent(this, ViewPassword::class.java)
        startActivity(intent)
    }
}
