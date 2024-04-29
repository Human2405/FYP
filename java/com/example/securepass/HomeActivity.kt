package com.example.securepass

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // Initialize the keystoreManager when the HomeActivity is created
        SaveCreditCardActivity.initializeKeystoreManager(this)
    }

    //navigate to Login screen
    fun onLogout(v:View){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // navigate to view password screen
    fun openSavePasswordScreen(view: View) {
        val intent = Intent(this, ViewPassword::class.java)
        startActivity(intent)
    }

    //navigate to view note screen
    fun openNoteScreen(view: View){
        val intent = Intent(this, ViewNote::class.java)
        startActivity(intent)
    }

    //navigate to view card screen
    fun openCardScreen(view: View){
        val intent = Intent(this, ViewCard::class.java)
        startActivity(intent)
    }

    //navigate to view Identity screen
    fun openIDScreen(view: View){
        val intent = Intent(this, ViewIdentity::class.java)
        startActivity(intent)
    }

    //navigate to save generate password screen
    fun openGeneratePasswordScreen(view: View){
        val intent = Intent(this, GeneratePasswordActivity::class.java)
        startActivity(intent)
    }

    //navigate to save test password strength screen
    fun openTestPasswordStrengthScreen(view: View){
        val intent = Intent(this, TestPasswordStrength::class.java)
        startActivity(intent)
    }

    //navigate to activity monitor screen
    fun openActivityMonitorScreen(view: View){
        val intent = Intent(this, ViewActivityMonitor::class.java)
        startActivity(intent)
    }

    //navigate to profile screen
    fun openProfileActivity(view: View){
        val intent = Intent(this, profileActivity::class.java)
        startActivity(intent)
    }
}
