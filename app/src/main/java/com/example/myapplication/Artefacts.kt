package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Artefacts : Fragment() {
    private val viewModel: ArtefactsViewModel by viewModels()
    private lateinit var adapter: ArtefactAdapter
    private val fireStore = FirebaseFirestore.getInstance()
    private var dataFetched = false
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Artefacts", "onViewCreated")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Artefacts", "onResume")
        if (viewModel.artefactList.isEmpty()) {
            fetchArtefacts(requireView())
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("Artefacts", "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Artefacts", "onDestroyView")
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.artefacts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ArtefactAdapter(requireContext(), viewModel.artefactList, viewModel)

        adapter.setOnItemClickListener(object : ArtefactAdapter.OnItemClickListener {
            override fun onItemClick(artefact: Artefact) {
                val action = ArtefactsDirections.actionNavigationArtefactsToNavigationArtefactDetailView(artefact)
                Navigation.findNavController(view).navigate(action)
            }
        })

        recyclerView.adapter = adapter
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchArtefacts(view: View) {
        Snackbar.make(view, "Fetching Artefacts", Snackbar.LENGTH_SHORT).show()
        Log.d("Artefacts", "fetchArtefacts")

        GlobalScope.launch(Dispatchers.Main) {
            val deferred = async(Dispatchers.IO) {
                fireStore.collection("artefacts").get().await()
            }
            val artefactCollection = deferred.await()

            viewModel.clearArtefacts() // Clear existing data in the ViewModel
            for (document in artefactCollection.documents) {
                val artefact = Artefact(document)
                viewModel.addArtefact(artefact)
            }

            adapter.notifyItemRangeInserted(0, viewModel.artefactList.size)
            dataFetched = true
        }
    }
}

class ArtefactsViewModel : ViewModel() {
    val artefactList: MutableList<Artefact> = mutableListOf()

    fun addArtefact(artefact: Artefact) {
        artefactList.add(artefact)
    }

    fun clearArtefacts() {
        artefactList.clear()
    }
}