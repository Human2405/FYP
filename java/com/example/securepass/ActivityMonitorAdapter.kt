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
import com.example.securepass.ActivityMonitor
import com.example.securepass.Notes
import com.example.securepass.Password
import com.example.securepass.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityMonitorAdapter : RecyclerView.Adapter<ActivityMonitorAdapter.userTimeViewHolder>() {

    private val userTimes: MutableList<ActivityMonitor> = mutableListOf()

    // This method is responsible for creating instances of the ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): userTimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.logintime_item, parent, false)
        return userTimeViewHolder(view)
    }

    // This method is called to bind the data to the views of the ViewHolder.
    override fun onBindViewHolder(holder: userTimeViewHolder, position: Int) {
        val userTime = userTimes[position]
        holder.bind(userTime)
    }

    override fun getItemCount(): Int {
        return userTimes.size
    }

    // Add a method to update the list of passwords
    fun updateuserTimes(newuserTimes: List<ActivityMonitor>) {
        userTimes.clear()
        userTimes.addAll(newuserTimes)
        notifyDataSetChanged()
    }

    inner class userTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userTimeTitleTextView: TextView = itemView.findViewById(R.id.timeTitleTextView)

        fun bind(userTimes: ActivityMonitor) {
            userTimeTitleTextView.text = "Time: ${userTimes.userTime}"

        }

    }

}
