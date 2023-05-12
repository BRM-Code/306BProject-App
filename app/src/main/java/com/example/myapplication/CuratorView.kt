package com.example.myapplication

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentCuratorBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class CuratorView : Fragment() {
    private lateinit var curatorViewAdapter: CuratorViewAdapter
    private val pendingSuggestions: MutableList<Suggestion> = mutableListOf()
    private var _binding: FragmentCuratorBinding? = null
    private val binding get() = _binding!!
    private val collectionRef = FirebaseFirestore.getInstance().collection("suggestions")

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
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // Specify the swipe directions
                return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            }

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
                    ItemTouchHelper.LEFT ->  denySuggestion(suggestion)
                    ItemTouchHelper.RIGHT -> approveSuggestion(suggestion)
                }
                curatorViewAdapter.notifyItemChanged(position) // Notify the adapter that the item has changed
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val backgroundDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    if (dX > 0) R.drawable.swipe_approve_bg else R.drawable.swipe_deny_bg
                )
                backgroundDrawable?.setBounds(
                    viewHolder.itemView.left,
                    viewHolder.itemView.top,
                    viewHolder.itemView.right,
                    viewHolder.itemView.bottom
                )
                backgroundDrawable?.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
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
        Snackbar.make(binding.root, "Suggestion approved, go implement!", Snackbar.LENGTH_SHORT).show()
        removeSuggestion(suggestion)
        acceptSuggestion(suggestion)
    }

    private fun denySuggestion(suggestion: Suggestion) {
        Snackbar.make(binding.root, "Suggestion denied", Snackbar.LENGTH_SHORT).show()
        removeSuggestion(suggestion)
        deleteSuggestion(suggestion)
    }

    private fun removeSuggestion(suggestion: Suggestion) {
        pendingSuggestions.remove(suggestion)
        curatorViewAdapter.notifyDataSetChanged()
    }

    private fun acceptSuggestion(suggestion: Suggestion) {
        collectionRef.whereEqualTo("suggestion", suggestion.suggestion)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.update("isPending", false)
                        .addOnSuccessListener {Log.d(tag, "Document updated successfully") }
                        .addOnFailureListener { e -> Log.w(tag, "Error updating document", e) }
                }
            }
            .addOnFailureListener { e -> Log.w(tag, "Error getting documents", e) }
    }

    private fun deleteSuggestion(suggestion: Suggestion) {
        collectionRef.whereEqualTo("suggestion", suggestion.suggestion)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.delete()
                        .addOnSuccessListener { Log.d(tag, "Document deleted successfully") }
                        .addOnFailureListener { e -> Log.w(tag, "Error deleted document", e) }
                }
            }
            .addOnFailureListener { e -> Log.w(tag, "Error getting documents", e) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
