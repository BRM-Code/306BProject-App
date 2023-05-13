package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class AccountStore : ViewModel() {
    private var suggestions = mutableListOf<Suggestion>()
    private val db = FirebaseFirestore.getInstance().collection("suggestions")
    private val tag = "AccountStore"

    fun fetchSuggestions(): Task<QuerySnapshot> {
        val userEmail = Firebase.auth.currentUser?.email.toString()
        Log.d(tag, "Fetching suggestions for $userEmail")

        return db.whereEqualTo("username", userEmail).get()
    }

    fun getSuggestions(): MutableList<Suggestion> {
        Log.d(tag, "Getting suggestions")

        if (Firebase.auth.currentUser == null) {
            Log.d(tag, "User is not logged in, clearing suggestions")
            clearSuggestions()
            return suggestions
        }

        if (suggestions.isEmpty()) {
            Log.d(tag, "Suggestions list is empty, fetching suggestions")
            suggestions.clear()
            fetchSuggestions().addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val suggestion = document.toObject(Suggestion::class.java)
                    suggestion?.let { suggestions.add(it) }
                }
            }
                .addOnFailureListener { exception ->
                Log.d(tag, "Fetching suggestions failed", exception)
            }
        }

        return suggestions
    }

    fun submitSuggestion(suggestion: Suggestion){
        Log.d(tag, "Submitting suggestion")

        // Convert suggestion to HashMap
        val suggestionData = hashMapOf(
            "suggestion" to suggestion.suggestion,
            "timestamp" to suggestion.timestamp,
            "username" to suggestion.userName,
            "isPending" to true
        )

        // Add suggestion to Firebase Firestore
        db.add(suggestionData).addOnSuccessListener { documentReference ->
                Log.d("AccountStore", "suggestion added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Error submitting suggestion", e)
            }
    }

    fun clearSuggestions() {
        suggestions.clear()
    }

    companion object {
        private var instance: AccountStore? = null

        fun getInstance(): AccountStore {
            return instance ?: synchronized(this) {
                instance ?: AccountStore().also { instance = it }
            }
        }
    }
}

class Suggestion() {
    var suggestion: String? = null
    var timestamp: Timestamp? = null
    var userName: String? = null

    constructor(suggestion: String, timestamp: Timestamp, userName: String) : this() {
        this.suggestion = suggestion
        this.timestamp = timestamp
        this.userName = userName
    }

    fun timestampToString(): String {
        val date = java.sql.Date(timestamp!!.seconds * 1000 + timestamp!!.nanoseconds / 1000000)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }
}
