package com.example.securepass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
//import com.google.crypto.tink.config.TinkConfig
//import com.google.crypto.tink.proto.Tink
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lokile.encrypter.encrypters.EncryptedData
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.lokile.encrypter.encrypters.imp.Encrypter

class SaveCreditCardActivity : AppCompatActivity() {


    // The keystoreManager property is a late-initialized variable used to manage the keystore.
    // It is marked with @SuppressLint("StaticFieldLeak") to suppress lint warning about potential memory leaks.
    // The initializeKeystoreManager method is used to initialize the keystoreManager property.
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var keystoreManager: KeystoreManager
            private set

        fun initializeKeystoreManager(context: Context) {
            keystoreManager = KeystoreManager(context)
        }
    }


    //private val keystoreManager = KeystoreManager("card_key_alias")

    //set layout to save_credit_card
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.save_credit_card)

        // Initialize the keystoreManager
        initializeKeystoreManager(this)


       // Initialize Room database
        /*database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "my_database"
        ).build()*/


    }

    //retrieve user's input by converting it to string and call the save to firestore function
    fun onSaveCard(v: View) {
        val cardNameEditText = findViewById<EditText>(R.id.cardNameEditText)
        val cardNumberEditText = findViewById<EditText>(R.id.cardNumberEditText)
        val cardCVCEditText = findViewById<EditText>(R.id.cardCVCEditText)
        val cardExpiryEditText = findViewById<EditText>(R.id.cardExpiryEditText)
        val cardPINEditText = findViewById<EditText>(R.id.cardPINEditText)

        val cardName = cardNameEditText.text.toString()
        val cardNumber = cardNumberEditText.text.toString()
        val cardCVC = cardCVCEditText.text.toString()
        val cardExpiry = cardExpiryEditText.text.toString()
        val cardPIN = cardPINEditText.text.toString()

        // Get the currently logged-in user's UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.e("Firestore", "user uid for save notes: $userId")

        if (userId != null) {
            // Save card information to Firestore
            saveCardToFirestore(userId, cardName, cardNumber, cardCVC, cardExpiry, cardPIN)

            // Save card information to local storage
           //saveCardToLocalDatabase(cardName, cardNumber, cardCVC, cardExpiry, cardPIN)

            // Optionally, you can provide feedback to the user
            Toast.makeText(this, "Card information saved successfully", Toast.LENGTH_SHORT).show()
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "Failed to save card information", Toast.LENGTH_SHORT).show()
            Log.e("Firestore", "User is not logged in")
        }
    }

    //save card to firestore after encryption
    private fun saveCardToFirestore(userId: String, cardName: String, cardNumber: String, cardCVC: String, cardExpiry: String, cardPIN: String) {
        val userDoc = FirebaseFirestore.getInstance().collection("cards").document(userId)

        val appContext = applicationContext

        // Use app context and alias
        val encrypter = Encrypter.Builder(appContext, "card_alias")
            .build()

        Log.d("EncryptionTest", "Original cardName: $cardName")
        Log.d("EncryptionTest", "Original cardNumber: $cardNumber")
        Log.d("EncryptionTest", "Original cardCVC: $cardCVC")
        Log.d("EncryptionTest", "Original cardExpiry: $cardExpiry")
        Log.d("EncryptionTest", "Original cardPIN: $cardPIN")

        // Encrypt card information before saving to Firestore
        val encryptedCardName: EncryptedData? = encrypter.encryptOrNull(cardName.toByteArray())
        val encryptedCardNumber: EncryptedData? = encrypter.encryptOrNull(cardNumber.toByteArray())
        val encryptedCardCVC: EncryptedData? = encrypter.encryptOrNull(cardCVC.toByteArray())
        val encryptedCardExpiry: EncryptedData? = encrypter.encryptOrNull(cardExpiry.toByteArray())
        val encryptedCardPIN: EncryptedData? = encrypter.encryptOrNull(cardPIN.toByteArray())

        if (encryptedCardName != null && encryptedCardNumber != null && encryptedCardCVC != null
            && encryptedCardExpiry != null && encryptedCardPIN != null
        ) {
            // Convert byte arrays to Base64 strings
            val base64CardName = encryptedCardName.toStringData()
            val base64CardNumber = encryptedCardNumber.toStringData()
            val base64CardCVC = encryptedCardCVC.toStringData()
            val base64CardExpiry = encryptedCardExpiry.toStringData()
            val base64CardPIN = encryptedCardPIN.toStringData()

            Log.d("EncryptionTest", "Encrypted cardName: $base64CardName")
            Log.d("EncryptionTest", "Encrypted cardNumber: $base64CardNumber")
            Log.d("EncryptionTest", "Encrypted cardCVC: $base64CardCVC")
            Log.d("EncryptionTest", "Encrypted cardExpiry: $base64CardExpiry")
            Log.d("EncryptionTest", "Encrypted cardPIN: $base64CardPIN")

            val cardData = mapOf(
                "cardholder" to base64CardName,
                "cardNumber" to base64CardNumber,
                "cvc" to base64CardCVC,
                "expiry" to base64CardExpiry,
                "pin" to base64CardPIN
            )

            userDoc.collection("user_cards").add(cardData)
                .addOnSuccessListener {
                    Log.e("Firestore", "Card data saved successfully")
                    val intent = Intent(this, ViewCard::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error saving card data: $e")
                }
        } else {
            Log.e("EncryptionTest", "Error encrypting card data")
        }
    }

    //return to ViewCard screen
    fun backButton(v: View){
        val intent = Intent(this, ViewCard::class.java)
        startActivity(intent)
    }
}