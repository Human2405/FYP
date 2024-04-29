import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.securepass.Notes
import com.example.securepass.Password
import com.example.securepass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PasswordAdapter : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {
    private  val TAG = "PasswordAdapter"
    private val passwords: MutableList<Password> = mutableListOf()
    private var onDeleteClickListener: ((position: Int) -> Unit)? = null
    private var onEditClickListener: ((position: Int, passwordId: String) -> Unit)? = null

    // Define a constant for the request code used when requesting internet permission.
    companion object {
        private const val REQUEST_CODE_INTERNET_PERMISSION = 1001
    }

    //return all the position of password
    fun getPassAtPosition(position: Int): Password {
        return passwords[position]
    }

    // This method is responsible for creating instances of the ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.password_item, parent, false)
        return PasswordViewHolder(view)
    }

    // This method is called to bind the data to the views of the ViewHolder.
    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val password = passwords[position]
        holder.bind(password)
    }

    //return total count of password
    override fun getItemCount(): Int {
        return passwords.size
    }

    // Add a method to update the list of passwords
    fun updatePasswords(newPasswords: List<Password>) {
        passwords.clear()
        passwords.addAll(newPasswords)
        notifyDataSetChanged()
    }

    // Set the onDeleteClickListener
    fun setOnDeleteClickListener(listener: (position: Int) -> Unit) {
        onDeleteClickListener = listener
    }

    // Set the onEditClickListener
    fun setOnEditClickListener(listener: (position: Int, passwordId: String) -> Unit) {
        onEditClickListener = listener
    }

    //get password's postion id for delete and edit function
    fun getPasswordIdForPosition(position: Int): String {
        if (position in 0 until passwords.size) {
            return passwords[position].passwordId
        } else {
            return ""
        }
    }

    //delete saved password
    fun deletePassword(position: Int, passwordId: String) {
        if (position >= 0 && position < passwords.size) {
            //val passwordToDelete = passwords[position]
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()

                // Delete the password document from Firestore
                firestore.collection("passwords")
                    .document(userId)
                    .collection("user_passwords")
                    .document(passwordId) // Use the obtained document ID
                    .delete()
                    .addOnSuccessListener {
                        // Password deleted successfully, now remove it from the adapter
                        passwords.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Log.e("Firestore", "Error deleting password: $e")
                    }
            }
        }
    }



    inner class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val passwordTextView: TextView = itemView.findViewById(R.id.passwordTextView)
        private val websiteTextView: TextView = itemView.findViewById((R.id.websiteTextView))
        private val urlTextView: TextView = itemView.findViewById((R.id.urlTextView))
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val editButton: Button = itemView.findViewById(R.id.editButton)


        //bind fetched password details with their respective TextView
        fun bind(password: Password) {
            usernameTextView.text = Html.fromHtml("<b>Username:</b> " + password.username)
            val passText = if (password.isContentVisible) password.password else "********"
            passwordTextView.text = Html.fromHtml("<b>Password:</b> $passText" )
            websiteTextView.text = Html.fromHtml("<b>Website:</b> " + password.websiteName)

            val url = "URL: " + password.url

            val spannableString = SpannableString(url)

            val internetPermission = android.Manifest.permission.INTERNET
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val urlWithoutPrefix = url.substring(5) // Remove "URL: " prefix...
                    Log.d(TAG, "onClick: $urlWithoutPrefix")

                    if (ContextCompat.checkSelfPermission(itemView.context, internetPermission) == PackageManager.PERMISSION_GRANTED) {
                        // Permission already granted, proceed to open the browser
                        openBrowser(urlWithoutPrefix, itemView.context)
                    } else {
                        // Permission not granted, request it
                        ActivityCompat.requestPermissions(itemView.context as Activity, arrayOf(internetPermission), REQUEST_CODE_INTERNET_PERMISSION)
                    }
                }
            }

            val startIndex = 5 // Start after "URL: "
            val endIndex = url.length // End at the end of the URL

            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            urlTextView.text = spannableString
            urlTextView.movementMethod = LinkMovementMethod.getInstance()

            // Delete Button
            deleteButton.setOnClickListener {
                val passwordId = password.passwordId
                deletePassword(adapterPosition, passwordId)
            }

            // Edit Button
            editButton.setOnClickListener {
                val passwordId = password.passwordId
                onEditClickListener?.invoke(adapterPosition, passwordId)
            }
        }

    }

    //open's browser the specific url from the saved password details when clicked
    private fun openBrowser(url: String, context: Context) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }


    // Find the position of the password in the list and update it
    fun updatePassword(updatedPassword: Password) {
        val position = passwords.indexOfFirst { it.passwordId == updatedPassword.passwordId }
        if (position != -1) {
            passwords[position] = updatedPassword
            notifyItemChanged(position)
        }
    }
}