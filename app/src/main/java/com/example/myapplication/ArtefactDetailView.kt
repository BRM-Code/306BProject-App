package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentArtefactviewBinding

class ArtefactDetailView : Fragment() {
    private var _binding: FragmentArtefactviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtefactviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val artefact = arguments?.get("ChosenArtefact") as Artefact

        // Use the artefact object to display information
        binding.artefactNameView.text = artefact.name
        binding.descShort.text = artefact.descriptionShort
        binding.descLong.text = artefact.descriptionLong
        binding.year.text = artefact.year
        binding.imageView.setImageBitmap(artefact.getImage())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
