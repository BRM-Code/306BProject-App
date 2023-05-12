package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AccountStore : ViewModel() {
    private var suggestions = mutableListOf<Suggestion>()
    private var userEmail = Firebase.auth.currentUser?.email.toString()
    private val db = FirebaseFirestore.getInstance().collection("suggestions").document(userEmail).collection("suggestions")
    private val tag = "AccountStore"

    fun fetchSuggestions() {
        Log.d(tag, "Fetching suggestions")
        db.get().addOnSuccessListener { documents ->
            documents.forEach { document ->
                val suggestion = document.getString("suggestion")
                val timestamp = document.getTimestamp("timestamp")

                if (suggestion != null && timestamp != null){
                    val suggestionObject = Suggestion(suggestion, timestamp)
                    suggestions.add(suggestionObject)
                    Log.d(tag, "Added suggestion: $suggestion with timestamp: $timestamp")
                }
            }
        }
        .addOnFailureListener { exception ->
            Log.d(tag, "get failed with ", exception)
        }
    }

    fun getSuggestions(): MutableList<Suggestion> {
        if (suggestions.isEmpty()) {
            fetchSuggestions()
        }
        return suggestions
    }

    fun submitSuggestion(suggestion: Suggestion){
        val suggestionData = hashMapOf(
            "suggestion" to suggestion.suggestion,
            "timestamp" to suggestion.timestamp
        )
        db.add(suggestionData)
            .addOnSuccessListener { documentReference ->
                Log.d("AccountStore", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Error adding document", e)
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

class Suggestion(suggestion: String, timestamp: Timestamp) {
    var suggestion : String? = suggestion
    var timestamp : Timestamp? = timestamp

}