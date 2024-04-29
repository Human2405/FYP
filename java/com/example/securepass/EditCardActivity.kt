package com.example.securepass

import CardAdapter
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


class EditCardActivity : AppCompatActivity() {

    //initialize adapter
    private lateinit var adapter: CardAdapter

    //set layout to edit_card and call fetch card details method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_card)

        adapter = CardAdapter()

        // Retrieve the card ID from the Intent
        val cardId = intent.getStringExtra("cardId")

        // Fetch the current card details and populate the EditText fields
        fetchCurrentCardDetails(cardId)

        val saveButton = findViewById<Button>(R.id.editSaveCardButton)
        saveButton.setOnClickListener {
            // Save the updated card details
            saveUpdatedCardDetails(cardId)
        }
    }

    //fetch saved details for the particular card that needs to be edited
    private fun fetchCurrentCardDetails(cardId: String?) {
        if (cardId != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val documentReference = firestore
                    .collection("cards")
                    .document(userId)
                    .collection("user_cards")
                    .document(cardId)

                // Use app context and alias to create an Encrypter instance
                val appContext = applicationContext
                val encrypter = Encrypter.Builder(appContext, "card_alias")
                    .build()

                documentReference.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            // Retrieve the encrypted card details from Firestore
                            val encryptedCardholderName = documentSnapshot.getString("cardholder")
                            val encryptedCardNumber = documentSnapshot.getString("cardNumber")
                            val encryptedCardCVC = documentSnapshot.getString("cvc")
                            val encryptedCardExpiry = documentSnapshot.getString("expiry")
                            val encryptedCardPIN = documentSnapshot.getString("pin")

                            // Decrypt the card details
                            val cardholderName = encryptedCardholderName?.let { encrypter.decryptOrNull(it) }
                            val cardNumber = encryptedCardNumber?.let { encrypter.decryptOrNull(it) }
                            val cardCVC = encryptedCardCVC?.let { encrypter.decryptOrNull(it) }
                            val cardExpiry = encryptedCardExpiry?.let { encrypter.decryptOrNull(it) }
                            val cardPIN = encryptedCardPIN?.let { encrypter.decryptOrNull(it) }

                            // Populate the EditText fields with the decrypted data
                            val cardholderNameEditText = findViewById<EditText>(R.id.editCardholderNameEditText)
                            val cardNumberEditText = findViewById<EditText>(R.id.editCardNumberEditText)
                            val cardCVCEditText = findViewById<EditText>(R.id.editCVCEditText)
                            val cardExpiryEditText = findViewById<EditText>(R.id.editCardExpiryEditText)
                            val cardPINEditText = findViewById<EditText>(R.id.editCardPINEditText)

                            cardholderNameEditText.setText(cardholderName?.toString() ?: "")
                            cardNumberEditText.setText(cardNumber?.toString() ?: "")
                            cardCVCEditText.setText(cardCVC?.toString() ?: "")
                            cardExpiryEditText.setText(cardExpiry?.toString() ?: "")
                            cardPINEditText.setText(cardPIN?.toString() ?: "")

                        } else {
                            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Toast.makeText(this, "Error fetching card details, try again later", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error fetching card details: $e")
                    }
            }
        }
    }


    //function save encrypted, updated card details to firestore
    private fun saveUpdatedCardDetails(cardId: String?) {

        val appContext = applicationContext
        val encrypter = Encrypter.Builder(appContext, "card_alias")
            .build()

        if (cardId != null) {
            // Retrieve the new data from the EditText fields.
            val updatedCardholderName = findViewById<EditText>(R.id.editCardholderNameEditText).text.toString()
            val updatedCardNumber = findViewById<EditText>(R.id.editCardNumberEditText).text.toString()
            val updatedCardCVC = findViewById<EditText>(R.id.editCVCEditText).text.toString()
            val updatedCardExpiry = findViewById<EditText>(R.id.editCardExpiryEditText).text.toString()
            val updatedCardPIN = findViewById<EditText>(R.id.editCardPINEditText).text.toString()

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val documentReference = firestore
                    .collection("cards")
                    .document(userId)
                    .collection("user_cards")
                    .document(cardId)

                // Encrypt the updated card details
                val encryptedCardName = encrypter.encryptOrNull(updatedCardholderName.toByteArray())
                val encryptedCardNumber = encrypter.encryptOrNull(updatedCardNumber.toByteArray())
                val encryptedCardCVC = encrypter.encryptOrNull(updatedCardCVC.toByteArray())
                val encryptedCardExpiry = encrypter.encryptOrNull(updatedCardExpiry.toByteArray())
                val encryptedCardPIN = encrypter.encryptOrNull(updatedCardPIN.toByteArray())

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

                    val updatedData = mapOf(
                        "cardholder" to base64CardName,
                        "cardNumber" to base64CardNumber,
                        "cvc" to base64CardCVC,
                        "expiry" to base64CardExpiry,
                        "pin" to base64CardPIN
                    )

                    documentReference.update(updatedData)
                        .addOnSuccessListener {

                            // Navigate back to the ViewCard activity
                            val updatedCard = Cards(cardId, updatedCardholderName, updatedCardNumber, updatedCardCVC, updatedCardNumber, updatedCardPIN)
                            adapter.updateCard(updatedCard)
                            Toast.makeText(this, "Card information updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ViewCard::class.java)
                            startActivity(intent)
                            Log.e("Firestore", "Successfully updated card details")
                        }
                        .addOnFailureListener { e ->
                            // Handle the error
                            Toast.makeText(this, "Failed to update card information", Toast.LENGTH_SHORT).show()
                            Log.e("Firestore", "Error updating card details: $e")
                        }
                } else {
                    Log.e("EncryptionTest", "Error encrypting updated card details")
                }
            }
        }
    }


    //return button to ViewCard
    fun editBackButton(v: View){
        val intent = Intent(this, ViewCard::class.java)
        startActivity(intent)
    }
}