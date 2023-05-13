package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.myapplication.databinding.FragmentQRScanBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class QRScan : Fragment() {
    private var _binding: FragmentQRScanBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val tag = QRScan::class.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQRScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.scanButton.setOnClickListener{
            scan(view)
        }
    }

    private fun scan(view: View) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC)
            .build()

        val scanner = GmsBarcodeScanning.getClient(requireContext(), options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                Snackbar.make(view, "Scanned!", Snackbar.LENGTH_SHORT).show()
                val rawValue: String? = barcode.rawValue
                findArtefact(rawValue, view)
                BadgeStore.getInstance().setBadgeUnlocked("timeLord", view)
            }
            .addOnFailureListener { e ->
                Snackbar.make(view, "Scan Failed", Snackbar.LENGTH_SHORT).show()
                Log.d(tag, e.toString())
            }
    }

    private fun findArtefact(rawValue: String?, view: View) {
        Log.d(tag, "Searching for artefact with ID: $rawValue")

        db.collection("artefacts")
            .document(rawValue.toString())
            .get().addOnSuccessListener { document ->
            if (document != null) {
                Log.d(tag, "DocumentSnapshot data: ${document.data}")

                // Navigate to ArtefactDetailView
                val action = ArtefactsDirections.actionNavigationArtefactsToNavigationArtefactDetailView(Artefact(document))
                Navigation.findNavController(view).navigate(action)
            } else {
                Log.d(tag, "No such document")
                Snackbar.make(view, "Invalid QR code", Snackbar.LENGTH_SHORT).show()
            }
        }
            .addOnFailureListener { exception ->
                Log.d(tag, "get failed with ", exception)
            }
    }
}
