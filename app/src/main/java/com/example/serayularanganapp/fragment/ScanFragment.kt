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
import com.example.serayularanganapp.model.VisitorData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var codeScanner: CodeScanner
    private lateinit var today: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data hari ini
        val currentDateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        today = currentDateString

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
        val databaseReference = FirebaseDatabase.getInstance().getReference("Wisata")

        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val tourId = tourData.id ?: return

        // Membuat referensi ke node visitorData pada tourData yang sesuai
        val visitorDataRef = databaseReference.child(tourId).child("visitorData").child(today)

        visitorDataRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentVisitorData = mutableData.getValue(VisitorData::class.java)

                // Jika data pengunjung belum ada, inisialisasi dengan nilai awal
                if (currentVisitorData == null) {
                    mutableData.value = VisitorData(today, 1, 1)
                } else {
                    // Jika data pengunjung sudah ada, tambahkan jumlah pengunjung
                    currentVisitorData.visitorCount++
                    currentVisitorData.totalVisitor++
                    mutableData.value = currentVisitorData
                }

                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (databaseError != null) {
                    // Tangani kesalahan
                    Toast.makeText(requireContext(), "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
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
