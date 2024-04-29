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
import com.example.securepass.Notes
import com.example.securepass.Password
import com.example.securepass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val notes: MutableList<Notes> = mutableListOf()
    private var onDeleteClickListener: ((position: Int) -> Unit)? = null
    private var onEditClickListener: ((position: Int, noteId: String) -> Unit)? = null


    //return all the position of notes
    fun getNoteAtPosition(position: Int): Notes {
        return notes[position]
    }

    // This method is responsible for creating instances of the ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item2, parent, false)
        return NoteViewHolder(view)
    }

    // This method is called to bind the data to the views of the ViewHolder.
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
    }

    //return total count of notes
    override fun getItemCount(): Int {
        return notes.size
    }

    // Add a method to update the list of passwords
    fun updateNotes(newNotes: List<Notes>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    // Set the onDeleteClickListener
    fun setOnDeleteClickListener(listener: (position: Int) -> Unit) {
        onDeleteClickListener = listener
    }

    // Set the onEditClickListener
    fun setOnEditClickListener(listener: (position: Int, noteId: String) -> Unit) {
        onEditClickListener = listener
    }

    //return the the postion's note id for delete and edit function
    fun getNoteIdForPosition(position: Int): String {
        if (position in 0 until notes.size) {
            return notes[position].noteId
        } else {
            return ""
        }
    }

    //delete saved note
    fun deleteNote(position: Int, noteId: String) {
        if (position >= 0 && position < notes.size) {
            //val passwordToDelete = passwords[position]
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()

                // Delete the password document from Firestore
                firestore.collection("notes")
                    .document(userId)
                    .collection("user_notes")
                    .document(noteId) // Use the obtained document ID
                    .delete()
                    .addOnSuccessListener {
                        // Password deleted successfully, now remove it from the adapter
                        notes.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        Log.e("Firestore", "Error deleting note: $e")
                    }
            }
        }
    }



    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val noteTitleTextView: TextView = itemView.findViewById(R.id.noteTitleTextView)
        private val noteContentTextView: TextView = itemView.findViewById(R.id.noteContentTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.noteDeleteButton)
        private val editButton: Button = itemView.findViewById(R.id.noteEditButton)


        //bind fetched note details to their respective TextView
        fun bind(notes: Notes) {
            noteTitleTextView.text = Html.fromHtml("<b>Title:</b> " + notes.noteTitle)
            val contentText = if (notes.isContentVisible) notes.noteContent else "********"
            noteContentTextView.text = Html.fromHtml("<b>Content:</b> $contentText")

            // Delete Button
            deleteButton.setOnClickListener {
                val noteId = notes.noteId
                deleteNote(adapterPosition, noteId)
            }

            // Edit Button
            editButton.setOnClickListener {
                val noteId = notes.noteId
                onEditClickListener?.invoke(adapterPosition, noteId)
            }
        }

    }

    // Find the position of the password in the list and update it
    fun updateNote(updatedNote: Notes) {
        val position = notes.indexOfFirst { it.noteId == updatedNote.noteId }
        if (position != -1) {
            notes[position] = updatedNote
            notifyItemChanged(position)
        }
    }
}
