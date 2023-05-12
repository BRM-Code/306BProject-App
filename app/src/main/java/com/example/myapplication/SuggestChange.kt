package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentSuggestChangeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SuggestChange : Fragment() {
    private var _binding: FragmentSuggestChangeBinding? = null
    private val binding get() = _binding!!
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
        setupRecyclerView(view)
        refresh()

        binding.accountButton.setOnClickListener {
            login()
            if(user != null) {
                refresh()
            }
        }
        binding.submitButton.setOnClickListener {
            if (user == null) {
                Snackbar.make(view, "Please log in to submit a suggestion", Snackbar.LENGTH_LONG).show()
            } else{
                submit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.pastSubmissionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = SuggestionAdapter()
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
        setupRecyclerView(requireView())
    }

    private fun login() {
        Log.d(tag, "Opening Login Page")
        val loginSheetPopup = LoginSheet()
        loginSheetPopup.show(parentFragmentManager, "loginSheetPopup")
    }

    private fun submit(){
        //Check suggestion is valid
        val suggestionText = binding.submitBox.text.toString()
        if (suggestionText == "") return

        //Submit suggestion
        val suggestion = Suggestion(suggestionText, com.google.firebase.Timestamp.now())
        AccountStore.getInstance().submitSuggestion(suggestion)

        //Update UI
        binding.submitBox.text = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
