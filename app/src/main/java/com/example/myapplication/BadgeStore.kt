package com.example.myapplication

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BadgeStore : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var badgeList: List<Badge>? = null

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
        badgeList = mutableListOf()

        viewModelScope.launch(Dispatchers.Main) {
            try {
                val snapshot = firestore.collection("badges").document(user).get().await()

                for (name in badgeNameList) {
                    snapshot.get(name)?.let {
                        (badgeList as MutableList<Badge>).add(Badge(name, it as Boolean))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getBadgeList(): List<Badge> {
        return badgeList!!
    }

    fun setBadgeUnlocked(badgeName : String, view : View) {
        //check if badge is already unlocked
        for (badge in badgeList!!) {
            if (badge.name == badgeName) {
                if (badge.isUnlocked) {
                    return
                }
                else {
                    badge.isUnlocked = true
                    Snackbar.make(view, "Badge Unlocked!: $badgeName", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // Update the corresponding badge document in Firestore
        viewModelScope.launch(Dispatchers.Main) {
            val data = hashMapOf(
                "name" to badgeName,
                "isUnlocked" to true
            )
            val documentId = Firebase.auth.currentUser?.email.toString()
            firestore.collection("badges").document(documentId).set(data).await()
        }
    }

    fun unlockAnotherUsersBadge(badgeName : String, userEmail : String) {
        // Update the corresponding badge document in Firestore
        viewModelScope.launch(Dispatchers.Main) {
            val data = hashMapOf(
                "name" to badgeName,
                "isUnlocked" to true
            )
            firestore.collection("badges").document(userEmail).set(data).await()
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