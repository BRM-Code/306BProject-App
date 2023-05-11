package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Artefacts : Fragment() {
    private lateinit var adapter: ArtefactAdapter
    private val artefacts = ArtefactsViewModel.getInstance()

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
                val action = ArtefactsDirections.actionNavigationArtefactsToNavigationArtefactDetailView(artefact)
                Navigation.findNavController(view).navigate(action)
            }
        })

        recyclerView.adapter = adapter
    }

}

class ArtefactsViewModel : ViewModel() {
    private val artefactList: MutableList<Artefact> = mutableListOf()
    private val fireStore = FirebaseFirestore.getInstance()

    private fun addArtefact(artefact: Artefact) {
        artefactList.add(artefact)
    }

    private fun clearArtefacts() {
        artefactList.clear()
    }

    fun getArtefactList(): List<Artefact> {
        if (artefactList.isEmpty()) {
            Log.d("Artefacts", "getArtefactList: artefactList is empty")
            fetchArtefacts()
        }
        return artefactList
    }

    fun getArtefactListSortedByYear(): List<Artefact> {
        return artefactList.sortedBy { it.year }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun fetchArtefacts() {
        Log.d("Artefacts", "fetchArtefacts")

        GlobalScope.launch(Dispatchers.Main) {
            val deferred = async(Dispatchers.IO) {
                fireStore.collection("artefacts").get().await()
            }
            val artefactCollection = deferred.await()

            clearArtefacts()
            for (document in artefactCollection.documents) {
                val artefact = Artefact(document)
                addArtefact(artefact)
            }
        }
    }

    companion object {
        private var instance: ArtefactsViewModel? = null

        fun getInstance(): ArtefactsViewModel {
            return instance ?: synchronized(this) {
                instance ?: ArtefactsViewModel().also { instance = it }
            }
        }
    }
}
