package com.example.securepass

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class GeneratePasswordActivity : AppCompatActivity() {

    //set layout to generate_password and get button and text id
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.generate_password)

        val generatePasswordButton = findViewById<Button>(R.id.generatePasswordButton)
        val generatedPasswordEditText = findViewById<EditText>(R.id.generatedPasswordEditText)

        generatePasswordButton.setOnClickListener {
            // Generate a password
            val generatedPassword = generatePassword(
                length = 12,  //Length of password generated
                includeUppercase = true,
                includeLowercase = true,
                includeDigits = true,
                includeSpecialChars = true
            )

            // Set the generated password in the EditText
            generatedPasswordEditText.setText(generatedPassword)
        }
    }

    //back button to return to Home screen
    fun backButton(v: View){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }


    // Function to generate a random password based on criteria
    private fun generatePassword(
        length: Int,
        includeUppercase: Boolean,
        includeLowercase: Boolean,
        includeDigits: Boolean,
        includeSpecialChars: Boolean
    ): String {
        // Define character sets based on criteria
        val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
        val digitChars = "0123456789"
        val specialChars = "!@#$%^&*()-_=+[]{}|;:'\"<>,.?/\\"

        // Create a character pool based on selected criteria
        val charPool = StringBuilder()
        if (includeUppercase) charPool.append(uppercaseChars)
        if (includeLowercase) charPool.append(lowercaseChars)
        if (includeDigits) charPool.append(digitChars)
        if (includeSpecialChars) charPool.append(specialChars)

        // Generate a random password by selecting characters from the pool
        return (1..length)
            .map { charPool.random() }
            .joinToString("")
    }
}
