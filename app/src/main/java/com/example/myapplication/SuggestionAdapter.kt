package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestionAdapter : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {
    private var suggestions: List<Suggestion> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val suggestionTextView: TextView = view.findViewById(R.id.suggestionTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.suggestionTextView.text = suggestion.suggestion
        holder.timestampTextView.text = suggestion.timestampToString()
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    fun submitList(suggestions: List<Suggestion>) {
        this.suggestions = suggestions
        notifyDataSetChanged()
    }
}
