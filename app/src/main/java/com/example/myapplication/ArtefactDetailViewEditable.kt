package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentArtefactviewEditableBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ArtefactDetailViewEditable : Fragment() {
    private var _binding: FragmentArtefactviewEditableBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtefactviewEditableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Have to suppress deprecation warning because of API level 28
        @Suppress("DEPRECATION")
        val artefact = arguments?.get("ChosenArtefact") as Artefact

        // Use the artefact object to display information
        binding.imageView.setImageBitmap(artefact.getImage(this.requireContext()))

        if (savedInstanceState != null){
            // Restore the user-entered text from the Bundle
            val updatedName = savedInstanceState.getString("Name_TEXT")
            val updatedDescShort = savedInstanceState.getString("DescShort_TEXT")
            val updatedDescLong = savedInstanceState.getString("DescLong_TEXT")
            val updatedYear = savedInstanceState.getString("Year_TEXT")
            binding.eArtefactNameView.setText(updatedName)
            binding.eDescShort.setText(updatedDescShort)
            binding.eDescLong.setText(updatedDescLong)
            binding.eYear.setText(updatedYear)
        }
        else{
            // Set the values for the EditText views
            binding.eArtefactNameView.setText(artefact.name)
            binding.eDescShort.setText(artefact.descriptionShort)
            binding.eDescLong.setText(artefact.descriptionLong)
            binding.eYear.setText(artefact.year)
        }

        // Enable the EditText views for editing
        binding.eArtefactNameView.isEnabled = true
        binding.eDescShort.isEnabled = true
        binding.eDescLong.isEnabled = true
        binding.eYear.isEnabled = true

        // Add submit button click listener
        binding.submitChangesButton.setOnClickListener {
            // Retrieve updated values from the EditText views
            val updatedName = binding.eArtefactNameView.text.toString()
            val updatedDescShort = binding.eDescShort.text.toString()
            val updatedDescLong = binding.eDescLong.text.toString()
            val updatedYear = binding.eYear.text.toString()

            // Update the artefact in the database
            Firebase.firestore.collection("artefacts")
                .whereEqualTo("Name", artefact.name)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d("ArtefactDetailViewEditable", "Updating artefact ${artefact.name} in database")
                        document.reference.update("Name", updatedName)
                        document.reference.update("DescriptionShort", updatedDescShort)
                        document.reference.update("DescriptionLong", updatedDescLong)
                        document.reference.update("Year", updatedYear)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("ArtefactDetailViewEditable", "Error getting documents: ", exception)
                }

            // Update the artefact in the local store
            ArtefactsStore.getInstance().fetchArtefacts()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Saving the user-entered text into the Bundle
        val updatedName = binding.eArtefactNameView.text.toString()
        val updatedDescShort = binding.eDescShort.text.toString()
        val updatedDescLong = binding.eDescLong.text.toString()
        val updatedYear = binding.eYear.text.toString()
        outState.putString("Name_TEXT", updatedName)
        outState.putString("DescShort_TEXT", updatedDescShort)
        outState.putString("DescLong_TEXT", updatedDescLong)
        outState.putString("Year_TEXT", updatedYear)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
