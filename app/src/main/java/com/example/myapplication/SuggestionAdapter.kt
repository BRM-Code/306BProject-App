package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class SuggestionAdapter : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val suggestionTextView: TextView = view.findViewById(R.id.suggestionTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = AccountStore.getInstance().getSuggestions()[position]
        holder.suggestionTextView.text = suggestion.suggestion
        holder.timestampTextView.text = suggestion.timestamp?.let { formatTimestamp(it) }
    }

    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        val date = java.sql.Date(timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }

    override fun getItemCount(): Int {
        return AccountStore.getInstance().getSuggestions().size
    }
}