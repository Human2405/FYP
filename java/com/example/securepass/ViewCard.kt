package com.example.securepass

import CardAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.securepass.SaveCreditCardActivity.Companion.initializeKeystoreManager
//import com.example.securepass.SaveCreditCardActivity.Companion.keystoreManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lokile.encrypter.encrypters.imp.Encrypter
import kotlinx.coroutines.launch

class ViewCard : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardAdapter
    private var keystoreManager: KeystoreManager? = null

    //initialize firebase and set layout to view_card
    override fun onCreate(savedInstanceState: Bundle?) {
        initializeKeystoreManager(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_card)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.cardsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create an adapter with the empty card list
        adapter = CardAdapter()

        // Set the adapter for the RecyclerView
        recyclerView.adapter = adapter

        fetchCards()

        adapter.setOnDeleteClickListener { position ->
            val cardId = getCardIdForPosition(position) // Get the card ID for the clicked item
            deleteCard(position, cardId)
        }

        adapter.setOnEditClickListener { position, cardId ->
            editCard(position, cardId)
        }

    }

    //initialize android keystore
    private fun initializeKeystoreManager(context: Context) {
        if (this != null) {
            keystoreManager = KeystoreManager(this)
        } else {
            // Log or handle the case where the context is null
            Log.e("ViewCard", "Context is null during initialization")
        }
    }


    //start activity on EditCardActivity
    private fun editCard(position: Int, cardId: String) {
        // Here, you can start the EditCardActivity and pass the cardId to it.
        val intent = Intent(this, EditCardActivity::class.java)
        intent.putExtra("cardId", cardId)
        intent.putExtra("adapterPosition", position)
        startActivity(intent)
    }

    // This function is used to initiate the deletion of a card from the adapter.
    private fun deleteCard(position: Int, cardId: String) {
        adapter.deleteCard(position, cardId)
    }

    // This function is used to retrieve the card ID for a given position from the adapter.
    private fun getCardIdForPosition(position: Int): String {
        return adapter.getCardIdForPosition(position)
    }


    //retrieve saved cards from firebase and decrypts it
    private fun fetchCards() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Create a Firestore reference to the "user_cards" sub-collection
            val userCardsCollection = FirebaseFirestore.getInstance()
                .collection("cards")
                .document(userId)
                .collection("user_cards")

            // Use your app context and alias to create an Encrypter instance
            val appContext = applicationContext
            val encrypter = Encrypter.Builder(appContext, "card_alias")
                .build()

            // Fetch cards from Firestore and update the adapter
            userCardsCollection.get().addOnSuccessListener { documents ->
                val cardList = mutableListOf<Cards>()
                for (document in documents) {
                    val cardId = document.id // Retrieve the document ID

                    val cardholderName = document.getString("cardholder")?.let { encryptedData ->
                        Log.d("DecryptionTest", "Encrypted cardholderName: $encryptedData")
                        try {
                            val decryptedData = encrypter.decryptOrNull(encryptedData)
                            Log.d("DecryptionTest", "Decrypted cardholderName: $decryptedData")
                            decryptedData?.toString() ?: ""
                        } catch (e: Exception) {
                            Log.e("DecryptionTest", "Decryption error: ${e.message}")
                            ""
                        }
                    } ?: ""

                    val cardNumber = document.getString("cardNumber")?.let { encryptedData ->
                        val decryptedData = encrypter.decryptOrNull(encryptedData)
                        Log.d("DecryptionTest", "Decrypted cardNumber: $decryptedData")
                        decryptedData?.toString() ?: ""
                    } ?: ""

                    val cardCVC = document.getString("cvc")?.let { encryptedData ->
                        val decryptedData = encrypter.decryptOrNull(encryptedData)
                        Log.d("DecryptionTest", "Decrypted cardCVC: $decryptedData")
                        decryptedData?.toString() ?: ""
                    } ?: ""

                    val cardExpiry = document.getString("expiry")?.let { encryptedData ->
                        val decryptedData = encrypter.decryptOrNull(encryptedData)
                        Log.d("DecryptionTest", "Decrypted cardExpiry: $decryptedData")
                        decryptedData?.toString() ?: ""
                    } ?: ""

                    val cardPIN = document.getString("pin")?.let { encryptedData ->
                        val decryptedData = encrypter.decryptOrNull(encryptedData)
                        Log.d("DecryptionTest", "Decrypted cardPIN: $decryptedData")
                        decryptedData?.toString() ?: ""
                    } ?: ""

                    val cardItem = Cards(cardholderName, cardNumber, cardCVC, cardExpiry, cardPIN, cardId)
                    cardList.add(cardItem)
                }

                // Update the adapter with the fetched data
                adapter.updateCards(cardList)
            }.addOnFailureListener { exception ->
                // Handle Firestore query failure
                Log.e("Firestore", "Error querying Firestore: $exception")
                Toast.makeText(this, "Failed to retrieve credit card. Try later", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // This function is triggered on interaction to show or hide the content of an item in the RecyclerView.
    fun onShowCard(v: View) {
        val holder = recyclerView.findContainingViewHolder(v)

        if (holder != null && holder.adapterPosition != RecyclerView.NO_POSITION) {
            val position = holder.adapterPosition
            val note = adapter.getCardAtPosition(position)

            Log.d("ViewNote", "isContentVisible before toggle: ${note.isContentVisible}")

            // Toggle the visibility of the note content
            note.isContentVisible = !note.isContentVisible

            Log.d("ViewNote", "isContentVisible after toggle: ${note.isContentVisible}")

            // Notify the adapter that the data has changed
            adapter.notifyItemChanged(position)
        }
    }

    //return to home screen
    fun backButton(v: View){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    //start activity on SaveCreditCardActivity to save a new card
    fun onNewCard(v : View){
        val intent = Intent(this, SaveCreditCardActivity::class.java)
        startActivity(intent)
    }
}
