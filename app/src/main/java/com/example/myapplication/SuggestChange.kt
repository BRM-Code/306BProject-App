package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentSuggestChangeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class SuggestChange : Fragment() {
    private var _binding: FragmentSuggestChangeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private var user = Firebase.auth.currentUser
    private val tag = "SuggestChange"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuggestChangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()

        binding.accountButton.setOnClickListener {
            login()
            if(user != null) {
                refresh()
            }
        }
        binding.submitButton.setOnClickListener {
            if (user == null) {
                login()
                submit()
                refresh()
            } else{
                submit()
                updateRecyclerView()
            }
        }
    }

    private fun refresh() {
        Log.d(tag, "Refreshing")
        // Check if user is logged in
        user = Firebase.auth.currentUser
        if (user != null) {
            // Set the text of the logged-in user's name
            val name = user!!.email.toString().substringBefore("@")
            binding.loggedInUserTextView.text = getString(R.string.logged_in_user, name)
        } else {
            // Set the text to ask the user to log in
            binding.loggedInUserTextView.text = getString(R.string.not_logged_in)
        }

        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        Log.d(tag, "Updating recycler view")
        val userEmail = user?.email

        val suggestionsRef = db.collection("suggestions").document(userEmail.toString()).collection("suggestions")

        val suggestionList = mutableListOf<String>()
        val timestampList = mutableListOf<String>()

        val adapter = SuggestionAdapter(suggestionList, timestampList)
        binding.pastSubmissionsRecyclerView.adapter = adapter
        binding.pastSubmissionsRecyclerView.layoutManager = LinearLayoutManager(context)

        suggestionsRef.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val suggestion = document.getString("suggestion")
                    val timestamp = document.getTimestamp("timestamp")

                    suggestionList.add(suggestion.toString())
                    timestamp?.let { formatTimestamp(it) }?.let { timestampList.add(it) }
                    Log.d(tag, "Added suggestion: $suggestion with timestamp: $timestamp")
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d(tag, "get failed with ", exception)
            }
    }

    private fun login() {
        Log.d(tag, "Opening Login Page")
        val loginSheetPopup = LoginSheet()
        loginSheetPopup.show(parentFragmentManager, "loginSheetPopup")
    }

    private fun submit(){
        //get the text from the text box
        val suggestion = binding.submitBox.text.toString()
        if (suggestion == "") {
            return
        }
        //get the user's email
        val timestamp = com.google.firebase.Timestamp.now()
        //send to firebase
        val suggestionData = hashMapOf(
            "suggestion" to suggestion,
            "timestamp" to timestamp
        )
        db.collection("suggestions").document(user?.email.toString()).collection("suggestions").add(suggestionData)
            .addOnSuccessListener { documentReference ->
                Log.d(tag, "DocumentSnapshot added with ID: ${documentReference.id}")
                binding.submitBox.text = null
                updateRecyclerView()
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Error adding document", e)
            }
    }

    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        val date = java.sql.Date(timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
