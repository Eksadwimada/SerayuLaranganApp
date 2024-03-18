package com.example.serayularanganapp.fragment

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.serayularanganapp.databinding.FragmentScanBinding
import com.example.serayularanganapp.model.TourData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null

    private val binding get() = _binding!!

    private lateinit var codeScanner: CodeScanner
    private lateinit var today: String

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ambil data hari ini
        today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        codeScanner()
        setPermission()
    }

    private fun codeScanner() {
        codeScanner = CodeScanner(requireContext(), binding.scanner)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                requireActivity().runOnUiThread {
                    showScanResultPopup(it.text)
                }
            }

            errorCallback = ErrorCallback {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }

            binding.scanner.setOnClickListener {
                codeScanner.startPreview()
            }
        }
    }

    private fun showScanResultPopup(scanResult: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Scan Result")
        builder.setMessage("Scan Result: $scanResult")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()

            // Ambil instance TourData yang sesuai dengan kode QR yang dipindai
            // Ambil data dari Firebase
            val databaseReference = FirebaseDatabase.getInstance().getReference("Wisata")
            databaseReference.child(scanResult).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val tourData = snapshot.getValue(TourData::class.java)
                        if (tourData != null) {
                            // Update the visitor count
                            updateVisitorCounts(tourData)
                        }
                    } else {
                        // Handle if TourData for the scanned QR code doesn't exist
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun updateVisitorCounts(tourData: TourData) {
        // Assuming you have a Firebase reference
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Wisata")

        // Update jumlah pengunjung hari ini
        val updatedTodayVisitors = (tourData.dailyVisitorCounts[today] ?: 0) + 1

        // Update total jumlah pengunjung wisata
        val updatedTotalVisitors = tourData.totalVisitor + 1

        val updateMap = mapOf(
            "dailyVisitorCounts/$today" to updatedTodayVisitors,
            "totalVisitors" to updatedTotalVisitors
        )

        // Update Firebase dengan jumlah pengunjung
        databaseReference.child(tourData.name!!).updateChildren(updateMap)
            .addOnSuccessListener {
                // Successfully updated visitor counts in Firebase
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.startPreview()
    }

    private fun setPermission() {
        val permission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeReq()
        }
    }

    private fun makeReq() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            101 -> {
                if (grantResults. isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(requireContext(), "Permission Dibutuhkan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}