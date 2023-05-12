package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentCuratorBinding
import com.google.firebase.firestore.FirebaseFirestore

class CuratorView : Fragment() {
    private lateinit var curatorViewAdapter: CuratorViewAdapter
    private val pendingSuggestions: MutableList<Suggestion> = mutableListOf()
    private var _binding: FragmentCuratorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuratorBinding.inflate(inflater, container, false)
        val view = binding.root
        fetchPendingSuggestions()

        // Set up the RecyclerView
        curatorViewAdapter = CuratorViewAdapter(this.requireContext(), pendingSuggestions)
        binding.suggestionList.adapter = curatorViewAdapter
        binding.suggestionList.layoutManager = LinearLayoutManager(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Not used in this case
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val suggestion = pendingSuggestions[position]

                // Perform action based on swipe direction
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        // Denied action
                        denySuggestion(suggestion)
                    }
                    ItemTouchHelper.RIGHT -> {
                        // Approved action
                        approveSuggestion(suggestion)
                    }
                }
                curatorViewAdapter.notifyItemChanged(position) // Notify the adapter that the item has changed
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.suggestionList)
    }

    private fun fetchPendingSuggestions() {
        Log.d("com.example.myapplication.CuratorView", "Fetching pending suggestions")

        // Query to get all pending suggestions
        FirebaseFirestore.getInstance().collection("suggestions")
            .whereEqualTo("isPending", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                pendingSuggestions.clear() // Clear the existing list before adding new suggestions
                for (document in querySnapshot.documents) {
                    val timestamp = document.getTimestamp("timestamp")
                    val userName = document.getString("username")
                    val suggestionText = document.getString("suggestion")

                    // Check for null values before adding the suggestion
                    if (timestamp != null && userName != null && suggestionText != null) {
                        pendingSuggestions.add(Suggestion(suggestionText, timestamp, userName))
                    }
                }
                curatorViewAdapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
            .addOnFailureListener { exception ->
                Log.w("com.example.myapplication.CuratorView", "Error getting documents: ", exception)
            }
    }

    private fun approveSuggestion(suggestion: Suggestion) {
        // TODO: Implement the logic for approving the suggestion
    }

    private fun denySuggestion(suggestion: Suggestion) {
        // TODO: Implement the logic for denying the suggestion
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
