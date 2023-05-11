package com.example.myapplication
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimelineAdapter : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val timelineItem = ArtefactsViewModel.getInstance().getArtefactListSortedByYear()[position]
        holder.bind(timelineItem)
    }

    override fun getItemCount(): Int {
        return ArtefactsViewModel.getInstance().getArtefactList().size
    }

    inner class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val yearTextView: TextView = itemView.findViewById(R.id.timelineItemYear)
        private val textTextView: TextView = itemView.findViewById(R.id.timelineItemText)

        fun bind(artefact: Artefact) {
            yearTextView.text = artefact.year
            textTextView.text = artefact.name
        }
    }
}
