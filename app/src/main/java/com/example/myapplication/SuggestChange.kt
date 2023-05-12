package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentSuggestChangeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SuggestChange : Fragment() {
    private var _binding: FragmentSuggestChangeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private val tag = "SuggestChange"
    private var isCurator: Boolean = false
    private lateinit var suggestionAdapter: SuggestionAdapter

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null) {
            val name = user.email.toString().substringBefore("@")
            binding.loggedInUserTextView.text = getString(R.string.logged_in_user, name)
        } else {
            binding.loggedInUserTextView.text = getString(R.string.not_logged_in)
            AccountStore.getInstance().clearSuggestions()
        }
        refresh()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuggestChangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        auth.addAuthStateListener(authStateListener)


        setupRecyclerView(view)
        checkCurator()

        binding.accountButton.setOnClickListener { login() }

        binding.submitButton.setOnClickListener {
            if (auth.currentUser == null) {
                Snackbar.make(view, "Please log in to submit a suggestion", Snackbar.LENGTH_LONG).show()
            } else {
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

        suggestionAdapter = SuggestionAdapter()
        recyclerView.adapter = suggestionAdapter
    }

    private fun refresh() {
        Log.d(tag, "Refreshing")

        if (auth.currentUser != null) {
            val name = auth.currentUser!!.email.toString().substringBefore("@")
            binding.loggedInUserTextView.text = getString(R.string.logged_in_user, name)
        } else {
            binding.loggedInUserTextView.text = getString(R.string.not_logged_in)
            AccountStore.getInstance().clearSuggestions()
        }

        val suggestions = AccountStore.getInstance().getSuggestions()
        suggestionAdapter.submitList(suggestions)
    }

    private fun login() {
        Log.d(tag, "Opening Login Page")
        val loginSheetPopup = LoginSheet()
        loginSheetPopup.show(parentFragmentManager, "loginSheetPopup")
    }

    private fun submit() {
        val suggestionText = binding.submitBox.text.toString()
        if (suggestionText.isBlank()) return

        val suggestion = Suggestion(suggestionText, com.google.firebase.Timestamp.now(), auth.currentUser!!.email.toString())
        AccountStore.getInstance().submitSuggestion(suggestion)

        binding.submitBox.text = null

        // Refresh the RecyclerView to show the new suggestion
        refresh()
    }

    private fun checkCurator() {
        Log.d(tag, "Checking if user is curator")
        FirebaseFirestore.getInstance().collection("curators").get().addOnSuccessListener { result ->
            val curators = result.documents.map { doc -> doc.id }
            isCurator = curators.contains(auth.currentUser?.email.toString())

            if (isCurator) {
                Log.d(tag, "User is curator")
                binding.curatorViewButton.visibility = View.VISIBLE
                binding.curatorViewButton.setOnClickListener {
                    val action = SuggestChangeDirections.actionNavigationSuggestChangeToCuratorView()
                    Navigation.findNavController(requireView()).navigate(action)
                }
            } else {
                Log.d(tag, "User is not curator")
                binding.curatorViewButton.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        auth.removeAuthStateListener(authStateListener)
    }
}


