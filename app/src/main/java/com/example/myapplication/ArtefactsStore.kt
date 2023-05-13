package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        private var instance: ArtefactsStore? = null

        fun getInstance(): ArtefactsStore {
            return instance ?: synchronized(this) {
                instance ?: ArtefactsStore().also { instance = it }
            }
        }
    }
}
