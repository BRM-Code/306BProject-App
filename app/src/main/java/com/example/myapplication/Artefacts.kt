package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
    private lateinit var adapter: ArtefactAdapter
    private val artefactList = mutableListOf<Artefact>()
    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_artefacts, container, false)
        setupRecyclerView(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        fetchArtefacts(requireView())
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.artefacts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ArtefactAdapter(requireContext(), artefactList)

        adapter.setOnItemClickListener(object : ArtefactAdapter.OnItemClickListener {
            override fun onItemClick(artefact: Artefact) {
                val artefactDetailView = ArtefactDetailView.newInstance(artefact)

                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.container, artefactDetailView)
                    ?.commit()
            }
        })

        recyclerView.adapter = adapter
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchArtefacts(view: View) {
        Snackbar.make(view, "Fetching Artefacts", Snackbar.LENGTH_SHORT).show()
        if (artefactList.size > 0) {
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            val deferred = async(Dispatchers.IO) {
                fireStore.collection("artefacts").get().await()
            }
            val artefactCollection = deferred.await()
            for (document in artefactCollection.documents) {
                val artefact = Artefact(document)
                artefactList.add(artefact)
            }
            adapter.notifyItemRangeInserted(0, artefactList.size)
        }
    }
}
