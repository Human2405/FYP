package com.example.securepass

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TestPasswordStrength : AppCompatActivity(){

    // tests and displays the strength of a password entered by the user
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_password_strength)

        val passwordInputField = findViewById<EditText>(R.id.passwordInputField)
        val testStrengthButton = findViewById<Button>(R.id.testStrengthButton)
        val strengthTextView = findViewById<TextView>(R.id.strengthTextView)

        testStrengthButton.setOnClickListener {
            val inputStrength = passwordInputField.text.toString()
            val passwordStrength = calculatePasswordStrength(inputStrength)
            strengthTextView.text = passwordStrength.toString()

            // Set text color based on password strength
            when (passwordStrength) {
                PasswordStrength.Weak -> strengthTextView.setTextColor(0xFF8B0000.toInt()) // Red
                PasswordStrength.Medium -> strengthTextView.setTextColor(0xFFFF8C00.toInt()) // Orange
                PasswordStrength.Strong -> strengthTextView.setTextColor(0xFF006400.toInt()) // Green
            }
        }

    }

    //return to home screen
    fun backButton(v: View){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }


    enum class PasswordStrength {
        Weak, Medium, Strong
    }

    //calculate the strength of password based on criteria
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        val minLength = 12
        val minUppercase = 1
        val minLowercase = 1
        val minDigits = 1
        val minSpecial = 1

        val uppercase = Regex("[A-Z]")
        val lowercase = Regex("[a-z]")
        val digits = Regex("[0-9]")
        val special = Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]")

        val hasUppercase = password.count { it.toString().matches(uppercase) } >= minUppercase
        val hasLowercase = password.count { it.toString().matches(lowercase) } >= minLowercase
        val hasDigits = password.count { it.toString().matches(digits) } >= minDigits
        val hasSpecial = password.count { it.toString().matches(special) } >= minSpecial

        val isStrong = hasDigits && hasSpecial

        return when {
            password.length < minLength -> PasswordStrength.Weak
            !hasUppercase || !hasLowercase -> PasswordStrength.Medium
            isStrong -> PasswordStrength.Strong
            else -> PasswordStrength.Medium
        }
    }
}