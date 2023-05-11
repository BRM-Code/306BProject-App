package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(DelicateCoroutinesApi::class)
class BadgeStore : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _badgeList = MutableLiveData<List<Badge>?>()
    val badgeList: MutableLiveData<List<Badge>?> = _badgeList

    private val user = Firebase.auth.currentUser?.email.toString()
    private val badgeNameList = listOf(
        "improvementImplemented",
        "readAll",
        "scanArtefact",
        "submittedImprovement",
        "timeLord"
    )

    init {
        refreshBadges()
    }

    fun refreshBadges() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("badges").document(user).get().await()
                val updatedBadgeList = mutableListOf<Badge>()

                for (name in badgeNameList) {
                    snapshot.get(name)?.let {
                        updatedBadgeList.add(Badge(name, it as Boolean))
                    }
                }

                _badgeList.postValue(updatedBadgeList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setBadgeUnlocked(index: Int, isUnlocked: Boolean) {
        // Update the corresponding badge in the list
        val currentList = _badgeList.value?.toMutableList()
        if (currentList != null && index in currentList.indices) {
            currentList[index].isUnlocked = isUnlocked
            _badgeList.postValue(currentList)

            // Update the corresponding badge document in Firestore
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val badge = currentList[index]
                    val documentId = "badge_$index" // Assuming each badge has a unique document ID
                    val data = hashMapOf(
                        "name" to badge.name,
                        "isUnlocked" to isUnlocked
                    )
                    firestore.collection("badges").document(documentId).set(data).await()
                } catch (e: Exception) {
                    // Handle any error that occurred during updating
                    e.printStackTrace()
                }
            }
        }
    }
    companion object {
        private var instance: BadgeStore? = null

        fun getInstance(): BadgeStore {
            return instance ?: synchronized(this) {
                instance ?: BadgeStore().also { instance = it }
            }
        }
    }
}

class Badge(val name: String, var isUnlocked: Boolean)