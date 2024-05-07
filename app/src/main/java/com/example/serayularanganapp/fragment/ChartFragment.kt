package com.example.serayularanganapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.example.serayularanganapp.databinding.FragmentChartBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChartFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        val view = binding.root

        val pie = AnyChart.pie()
        binding.pieChart.setChart(pie)

        val database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("TotalPengunjungPerUser")

        // Mendapatkan data dari Firebase
        getDataFromFirebase()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDataFromFirebase() {
        val wisataNames = arrayOf("Curug Ciputut", "River Tubing", "Tuk Pejaten", "Tuk Dandang", "Telakpak Wali")

        // Menginisialisasi total pengunjung untuk setiap wisata
        val totalPengunjungMap = mutableMapOf<String, Int>()

        // Listener untuk mengambil data dari Firebase
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Iterasi setiap wisata
                wisataNames.forEach { wisataName ->
                    // Ambil data total pengunjung dari Firebase untuk wisata saat ini
                    val wisataSnapshot = snapshot.child(wisataName)
                    var totalPengunjungWisata = 0

                    // Hitung total pengunjung untuk wisata ini
                    for (userSnapshot in wisataSnapshot.children) {
                        val userTotal = userSnapshot.child("total").getValue(Int::class.java) ?: 0
                        totalPengunjungWisata += userTotal
                    }

                    // Simpan total pengunjung untuk wisata ini
                    totalPengunjungMap[wisataName] = totalPengunjungWisata
                }

                // Setel data untuk PieChart setelah mendapatkan semua data dari Firebase
                setPieChartData(totalPengunjungMap)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle event dibatalkan
            }
        })
    }

    private fun setPieChartData(totalPengunjungMap: Map<String, Int>) {
        val pieEntries = ArrayList<DataEntry>()

        // Iterasi melalui map totalPengunjungMap dan tambahkan data ke pieEntries
        totalPengunjungMap.forEach { (wisataName, totalPengunjung) ->
            pieEntries.add(ValueDataEntry(wisataName, totalPengunjung))
        }

        // Buat objek PieChart
        val pie = AnyChart.pie()

        // Setel data ke PieChart
        pie.data(pieEntries)

        // Setel judul PieChart
        pie.title("Total Pengunjung per Wisata")

        // Setel PieChart ke tampilan
        binding.pieChart.setChart(pie)
    }

}