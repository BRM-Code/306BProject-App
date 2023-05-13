package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ArtefactsStore : ViewModel() {
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

    fun fetchArtefacts() {
        Log.d("Artefacts", "fetchArtefacts")

        viewModelScope.launch(Dispatchers.Main) {
            val artefactCollection = withContext(Dispatchers.IO) {
                fireStore.collection("artefacts").get().await()
            }

            clearArtefacts()
            for (document in artefactCollection.documents) {
                addArtefact(Artefact(document))
            }
        }
    }

    companion object {
        private var instance: ArtefactsStore? = null

        fun getInstance(): ArtefactsStore {
            return instance ?: synchronized(this) {
                instance ?: ArtefactsStore().also { instance = it }
            }
        }
    }
}
