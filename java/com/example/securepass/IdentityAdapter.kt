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
import com.example.securepass.Identity
import com.example.securepass.Notes
import com.example.securepass.Password
import com.example.securepass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IdentityAdapter : RecyclerView.Adapter<IdentityAdapter.IdentityViewHolder>() {
    private val identities: MutableList<Identity> = mutableListOf()
    private var onDeleteClickListener: ((position: Int) -> Unit)? = null
    private var onEditClickListener: ((position: Int, identityId: String) -> Unit)? = null


    //return all the position of ID
    fun getIDAtPosition(position: Int): Identity {
        return identities[position]
    }

    // This method is responsible for creating instances of the ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdentityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.identity_item, parent, false)
        return IdentityViewHolder(view)
    }

    // This method is called to bind the data to the views of the ViewHolder.
    override fun onBindViewHolder(holder: IdentityViewHolder, position: Int) {
        val identity = identities[position]
        holder.bind(identity)
    }

    //return total count of identities
    override fun getItemCount(): Int {
        return identities.size
    }

    // Add a method to update the list of passwords
    fun updateIdentities(newIdentities: List<Identity>) {
        identities.clear()
        identities.addAll(newIdentities)
        notifyDataSetChanged()
    }

    // Set the onDeleteClickListener
    fun setOnDeleteClickListener(listener: (position: Int) -> Unit) {
        onDeleteClickListener = listener
    }

    // Set the onEditClickListener
    fun setOnEditClickListener(listener: (position: Int, identityId: String) -> Unit) {
        onEditClickListener = listener
    }


    //return the id for that particular identity's position
    fun getIdentityIdForPosition(position: Int): String {
        if (position in 0 until identities.size) {
            return identities[position].identityId
        } else {
            return ""
        }
    }

    //delete saved identity
    fun deleteIdentity(position: Int, identityId: String) {
        if (position >= 0 && position < identities.size) {
            //val passwordToDelete = passwords[position]
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()

                // Delete the password document from Firestore
                firestore.collection("identities")
                    .document(userId)
                    .collection("user_identities")
                    .document(identityId) // Use the obtained document ID
                    .delete()
                    .addOnSuccessListener {
                        // Password deleted successfully, now remove it from the adapter
                        identities.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Log.e("Firestore", "Error deleting identity: $e")
                    }
            }
        }
    }



    inner class IdentityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameTextView)
        private val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameTextView)
        private val addressTextView: TextView = itemView.findViewById((R.id.addressTextView))
        private val IDNumberTextView: TextView = itemView.findViewById((R.id.IDNumberTextView))
        private val deleteButton: Button = itemView.findViewById(R.id.identityDeleteButton)
        private val editButton: Button = itemView.findViewById(R.id.identityEditButton)


        //bind the fetched identity to its respective textview
        fun bind(identity: Identity) {
            firstNameTextView.text = Html.fromHtml("<b>First Name:</b> " + identity.firstName)
            lastNameTextView.text = Html.fromHtml("<b>Last Name:</b> " + identity.lastName)

            //mask address
            val addressText = if (identity.isContentVisible) identity.address else "********"
            addressTextView.text = Html.fromHtml("<b>Address:</b>  $addressText")

            //mask id number
            val IDNumText = if (identity.isContentVisible) identity.IDNumber else "********"
            IDNumberTextView.text = Html.fromHtml("<b>ID:</b> $IDNumText")

            // Delete Button
            deleteButton.setOnClickListener {
                val identityId = identity.identityId
                deleteIdentity(adapterPosition, identityId)
            }

            // Edit Button
            editButton.setOnClickListener {
                val identityId = identity.identityId
                onEditClickListener?.invoke(adapterPosition, identityId)
            }
        }

    }

    // Find the position of the password in the list and update it
    fun updateIdentity(updatedIdentity: Identity) {
        val position = identities.indexOfFirst { it.identityId == updatedIdentity.identityId }
        if (position != -1) {
            identities[position] = updatedIdentity
            notifyItemChanged(position)
        }
    }
}