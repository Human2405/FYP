import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.securepass.Cards
import com.example.securepass.Password
import com.example.securepass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CardAdapter : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {
    private val cards: MutableList<Cards> = mutableListOf()
    private var onDeleteClickListener: ((position: Int) -> Unit)? = null
    private var onEditClickListener: ((position: Int, cardId: String) -> Unit)? = null

    fun getCardAtPosition(position: Int): Cards {
        return cards[position]
    }

    // This method is responsible for creating instances of the ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view)
    }

    // This method is called to bind the data to the views of the ViewHolder.
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card)
    }

    //return the total card quantity
    override fun getItemCount(): Int {
        return cards.size
    }

    // Add a method to update the list of passwords
    fun updateCards(newCards: List<Cards>) {
        cards.clear()
        cards.addAll(newCards)
        notifyDataSetChanged()
    }

    // Set the onDeleteClickListener
    fun setOnDeleteClickListener(listener: (position: Int) -> Unit) {
        onDeleteClickListener = listener
    }

    // Set the onEditClickListener
    fun setOnEditClickListener(listener: (position: Int, cardId: String) -> Unit) {
        onEditClickListener = listener
    }

    // get the card position/id for deletion and edit
    fun getCardIdForPosition(position: Int): String {
        if (position in 0 until cards.size) {
            return cards[position].cardId
        } else {
            return ""
        }
    }

    //delete card
    fun deleteCard(position: Int, cardId: String) {
        if (position >= 0 && position < cards.size) {
            //val passwordToDelete = passwords[position]
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()

                // Delete the password document from Firestore
                firestore.collection("cards")
                    .document(userId)
                    .collection("user_cards")
                    .document(cardId) // Use the obtained document ID
                    .delete()
                    .addOnSuccessListener {
                        // Password deleted successfully, now remove it from the adapter
                        cards.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Log.e("Firestore", "Error deleting card: $e")
                    }
            }
        }
    }



    //bind fetched result to TextView for user's view
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardholderTextView: TextView = itemView.findViewById(R.id.cardholderTextView)
        private val cardNumberTextView: TextView = itemView.findViewById(R.id.cardNumTextView)
        private val cardCVCTextView: TextView = itemView.findViewById((R.id.cvcTextView))
        private val cardExpiryTextView: TextView = itemView.findViewById((R.id.expiryDateTextView))
        private val cardPINTextView: TextView = itemView.findViewById((R.id.PINTextView))
        private val deleteButton: Button = itemView.findViewById(R.id.cardDeleteButton)
        private val editButton: Button = itemView.findViewById(R.id.cardEditButton)


        fun bind(cards: Cards) {
            cardholderTextView.text = Html.fromHtml("<b>Cardholder:</b> " + cards.cardholderName)

            //mask card number
            val cardNum = if (cards.isContentVisible) cards.cardNumber else "********"
            cardNumberTextView.text = Html.fromHtml("<b>Card Number:</b> $cardNum")

            //mask card cvc
            val card_cvc = if (cards.isContentVisible) cards.CVC else "********"
            cardCVCTextView.text = Html.fromHtml("<b>CVC:</b> $card_cvc")

            //mask card expiry
            val card_expiry = if (cards.isContentVisible) cards.cardExpiry else "****"
            cardExpiryTextView.text = Html.fromHtml("<b>Expiry:</b> $card_expiry")

            //mask card pin
            val card_pin = if (cards.isContentVisible) cards.cardPIN else "******"
            cardPINTextView.text = Html.fromHtml("<b>PIN:</b> $card_pin")

            // Delete Button
            deleteButton.setOnClickListener {
                val cardId = cards.cardId
                deleteCard(adapterPosition, cardId)
            }

            // Edit Button
            editButton.setOnClickListener {
                val cardId = cards.cardId
                onEditClickListener?.invoke(adapterPosition, cardId)
            }
        }

    }

    // Find the position of the password in the list and update it
    fun updateCard(updatedCard: Cards) {
        val position = cards.indexOfFirst { it.cardId == updatedCard.cardId }
        if (position != -1) {
            cards[position] = updatedCard
            notifyItemChanged(position)
        }
    }
}
