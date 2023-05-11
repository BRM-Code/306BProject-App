package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArtefactAdapter(
    private val context: Context,
    private val artefactList: List<Artefact>,
    private val viewModel: ArtefactsViewModel
) : RecyclerView.Adapter<ArtefactAdapter.ArtefactViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    class ArtefactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artefactName: TextView = itemView.findViewById(R.id.artefact_name)
        val artefactDescription: TextView = itemView.findViewById(R.id.artefact_descriptionShort)
        val artefactImage: ImageView = itemView.findViewById(R.id.artefact_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtefactViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_artefact, parent, false)
        return ArtefactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtefactViewHolder, position: Int) {
        val artefact = artefactList[position]
        holder.artefactName.text = artefact.name
        holder.artefactDescription.text = artefact.descriptionShort
        holder.artefactImage.setImageBitmap(artefact.getImage())

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(artefact)
        }
    }


    override fun getItemCount(): Int {
        return viewModel.artefactList.size
    }

    interface OnItemClickListener {
        fun onItemClick(artefact: Artefact)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

}
