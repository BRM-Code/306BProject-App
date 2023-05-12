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
        if (Firebase.auth.currentUser != null) {
            if (suggestions.isEmpty()) {
                fetchSuggestions().addOnSuccessListener { querySnapshot ->
                    suggestions.clear()
                    for (document in querySnapshot.documents) {
                        val suggestion = document.toObject(Suggestion::class.java)
                        suggestion?.let { suggestions.add(it) }
                    }
                    // Notify observers or update UI here
                    // For example, you can use LiveData or callbacks to notify observers or update UI
                }.addOnFailureListener { exception ->
                    Log.d(tag, "Fetching suggestions failed", exception)
                }
            }
        } else {
            clearSuggestions()
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
