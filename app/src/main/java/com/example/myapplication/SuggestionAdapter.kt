package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestionAdapter(private val suggestions: List<String>, private val timestamps: List<String>) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val suggestionTextView: TextView = view.findViewById(R.id.suggestionTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.suggestionTextView.text = suggestions[position]
        holder.timestampTextView.text = timestamps[position]
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }
}