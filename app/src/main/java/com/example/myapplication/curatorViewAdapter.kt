package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CuratorViewAdapter(private val context: Context, private val suggestions: List<Suggestion>) :
    RecyclerView.Adapter<CuratorViewAdapter.CuratorViewHolder>() {

    class CuratorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.textUser)
        val suggestionText: TextView = itemView.findViewById(R.id.textSuggestion)
        val timestampText: TextView = itemView.findViewById(R.id.textTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuratorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_curator_view, parent, false)
        return CuratorViewHolder(view)
    }

    override fun onBindViewHolder(holder: CuratorViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.userName.text = suggestion.userName
        holder.suggestionText.text = suggestion.suggestion
        holder.timestampText.text = suggestion.timestampToString()
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }
}
