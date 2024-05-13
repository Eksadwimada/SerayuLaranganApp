package com.example.serayularanganapp.fragment

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.serayularanganapp.R
import com.example.serayularanganapp.databinding.FragmentScanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var codeScanner: CodeScanner
    private lateinit var today: String
    private val allowedPlaces = listOf("Curug Ciputut", "River Tubing", "Tuk Pejaten", "Tuk Dandang", "Telakpak Wali")


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

        val currentDateString = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
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
                    Log.e("CodeScanner", "Camera initialization error: ${it.message}")
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
        builder.setTitle("Scan Berhasil!!!")

        if (allowedPlaces.contains(scanResult)) {
            builder.setMessage("Di wisata : $scanResult")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                saveVisitorData(scanResult)
            }
        } else {
            builder.setMessage("Anda hanya bisa melakukan scan di salah satu dari tempat wisata yang diizinkan.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            alertDialog.findViewById<TextView>(android.R.id.title)?.apply {
                setTextAppearance(requireContext(), R.style.AlertDialogTitle)
            }
            alertDialog.findViewById<TextView>(android.R.id.message)?.apply {
                setTextAppearance(requireContext(), R.style.AlertDialogMessage)
            }
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextAppearance(requireContext(), R.style.AlertDialogButton)
            }
        }
        alertDialog.show()
    }

    private fun saveVisitorData(scanResult: String) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().reference

            // Update jumlah pengunjung hari ini
            val jumlahHariIniRef = databaseReference.child("PengunjungHariIni").child(scanResult).child(today).child("HariIni")
            jumlahHariIniRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val jumlahHariIni = mutableData.getValue(Int::class.java) ?: 0
                    mutableData.value = jumlahHariIni + 1
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                    if (databaseError != null) {
                        Log.e("FirebaseError", "Transaction error: ${databaseError.message}")
                        Toast.makeText(requireContext(), "Transaction error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            val wisataNameRef = databaseReference.child("TotalPengunjungPerUser").child(scanResult).child(userId).child("name")
                            wisataNameRef.setValue(scanResult)
                        }
                    }
                }
            })

            // Update total pengunjung per user
            val totalUserRef = databaseReference.child("TotalPengunjungPerUser").child(scanResult).child(userId).child("total")
            totalUserRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val totalPengunjung = mutableData.getValue(Int::class.java) ?: 0
                    mutableData.value = totalPengunjung + 1
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                    if (databaseError != null) {
                        Log.e("FirebaseError", "Transaction error: ${databaseError.message}")
                        Toast.makeText(requireContext(), "Transaction error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Menambahkan jumlah pengunjung!", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        } else {
            Log.e("ScanFragment", "User belum Login!" )
            Toast.makeText(requireContext(), "User belum login!", Toast.LENGTH_SHORT).show()
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
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(requireContext(), "Permission Dibutuhkan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}