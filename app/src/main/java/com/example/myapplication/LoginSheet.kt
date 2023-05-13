package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.LoginBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginSheet : BottomSheetDialogFragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: LoginBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val authFailMessage = "Authentication failed."
    private val emailPassMessage = "Email and password must not be blank."

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginBottomSheetBinding.inflate(inflater, container, false)
        val view = binding.root
        auth = FirebaseAuth.getInstance()

        // If user is already logged in, hide login/register buttons and show logout button
        if (auth.currentUser != null) {
            binding.emailEditText.visibility = View.GONE
            binding.passwordEditText.visibility = View.GONE
            binding.loginButton.visibility = View.GONE
            binding.registerButton.visibility = View.GONE
            binding.loggedInUserTextView.visibility = View.VISIBLE
            binding.loggedInUserTextView.text = getString(R.string.logged_in_user, auth.currentUser!!.email.toString().substringBefore("@"))
            binding.logoutButton.visibility = View.VISIBLE

            binding.logoutButton.setOnClickListener {
                auth.signOut()
                AccountStore.getInstance().clearSuggestions()
                snackMessage("Logged out.")
            }
        }
        else {
            binding.loginButton.setOnClickListener {
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            AccountStore.getInstance().fetchSuggestions()
                            dismiss()
                        } else snackMessage(authFailMessage)
                    }
                } else snackMessage(emailPassMessage)
            }

            binding.registerButton.setOnClickListener {
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) dismiss() else snackMessage(authFailMessage)
                    }
                } else snackMessage(emailPassMessage)
            }
        }
        return view
    }

    private fun snackMessage(message: String) {
        parentFragment?.view?.let { it1 -> Snackbar.make(it1, message, Snackbar.LENGTH_SHORT).show() }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
