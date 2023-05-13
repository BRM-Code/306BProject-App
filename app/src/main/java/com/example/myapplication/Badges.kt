package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.FragmentBadgesBinding

class Badges : Fragment() {
    private var _binding: FragmentBadgesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_badges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBadgesBinding.bind(view)
        populateBadges(BadgeStore.getInstance().getBadgeList())
    }

    private fun populateBadges(badgeList: List<Badge>){
        Log.d("Badges", "Populating Badges")

        for (badge in badgeList) {
            when (badge.name) {
                "improvementImplemented" -> {
                    binding.ImproveBadge.text = getString(R.string.improvementImplemented, badgeText(badge))
                }
                "readAll" -> {
                    binding.ArtefactBadge.text = getString(R.string.readAll, badgeText(badge))
                }
                "scanArtefact" -> {
                    binding.ScanBadge.text = getString(R.string.scanArtefact, badgeText(badge))
                }
                "submittedImprovement" -> {
                    binding.ChangeBadge.text = getString(R.string.submittedImprovement, badgeText(badge))
                }
                "timeLord" -> {
                    binding.TimeLineBadge.text = getString(R.string.timeLord, badgeText(badge))
                }
                else -> {
                    Log.e("Badges", "${badge.name} not found")
                }
            }
        }
    }

    private fun badgeText(badge : Badge): String {
        return if (badge.isUnlocked){
            "Badge Unlocked: "
        } else{
            "Badge Locked: "
        }
    }
}
