package com.example.securepass

import ActivityMonitorAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ViewActivityMonitor : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityMonitorAdapter

    //initialize firebase ans set layout to view_logintime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_logintime)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.notesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create an adapter with the empty password list
        adapter = ActivityMonitorAdapter()

        // Set the adapter for the RecyclerView
        recyclerView.adapter = adapter

        // Fetch passwords from Firestore and update the adapter with the data
        fetchTimes()

    }

    //retrieve user's login time from firebase and set it in decending order to display the latest login time
    private fun fetchTimes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Create a Firestore reference to the "user_passwords" sub-collection
            val userTimesCollection = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("loginTimes")

            // Fetch passwords from Firestore and update the adapter
            userTimesCollection.orderBy("loginTime", Query.Direction.DESCENDING).get().addOnSuccessListener { documents ->
                Log.d("Firestore", "Number of documents: ${documents.size()}")
                val timeList = mutableListOf<ActivityMonitor>()
                for (document in documents) {
                    val timeId = document.id // Retrieve the document ID
                    //Log.d("Firestore", "Document ID: $timeId")

                    val userTime = document.getTimestamp("loginTime")?.toDate()
                    Log.d("Firestore", "time: $userTime")
                    if (userTime != null) {
                        val timeItem = ActivityMonitor(timeId, userTime)
                        timeList.add(timeItem)
                    }
                }

                // Update the adapter with the fetched data
                adapter.updateuserTimes(timeList)
            }.addOnFailureListener { exception ->
                // Handle Firestore query failure
                Log.e("Firestore", "Error querying Firestore: $exception")
                Toast.makeText(this, "Failed to retrieve data. Try later", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //return to home screen
    fun backButton(v: View){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}
