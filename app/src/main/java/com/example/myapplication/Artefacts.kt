package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Artefacts : Fragment() {
    private lateinit var adapter: ArtefactAdapter
    private val artefacts = ArtefactsStore.getInstance()
    private var isCurator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Artefacts", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Artefacts", "onCreateView")
        val view = inflater.inflate(R.layout.fragment_artefacts, container, false)
        setupRecyclerView(view)
        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.artefacts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ArtefactAdapter(requireContext(), artefacts.getArtefactList())

        adapter.setOnItemClickListener(object : ArtefactAdapter.OnItemClickListener {
            override fun onItemClick(artefact: Artefact) {
                Log.d(tag, "Checking if user is curator")
                FirebaseFirestore.getInstance().collection("curators").get().addOnSuccessListener { result ->
                    val curators = result.documents.map { doc -> doc.id }
                    isCurator = curators.contains(Firebase.auth.currentUser?.email.toString())

                    val action: NavDirections = if (isCurator) {
                        Log.d(tag, "User is curator")
                        ArtefactsDirections.actionNavigationArtefactsToArtefactDetailViewEditable(artefact)
                    } else {
                        Log.d(tag, "User is not curator")
                        ArtefactsDirections.actionNavigationArtefactsToNavigationArtefactDetailView(artefact)
                    }
                    Navigation.findNavController(view).navigate(action)
                }
            }
        })
        recyclerView.adapter = adapter
    }
}
