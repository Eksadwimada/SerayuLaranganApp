package com.example.serayularanganapp.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serayularanganapp.adapter.VisitsAdapter
import com.example.serayularanganapp.databinding.ActivityTouristVisitsBinding
import com.example.serayularanganapp.model.TourData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TouristVisitsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTouristVisitsBinding
    private lateinit var visitsAdapter: VisitsAdapter
    private lateinit var touristVisits: MutableList<TourData>
    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTouristVisitsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        touristVisits = mutableListOf()
        visitsAdapter = VisitsAdapter(touristVisits)
        binding.recyclerViewKunjungan.apply {
            layoutManager = LinearLayoutManager(this@TouristVisitsActivity)
            adapter = visitsAdapter
        }

        fetchVisitorVisitsFromFirebase()
    }

    private fun fetchVisitorVisitsFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("TotalPengunjungPerUser")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                touristVisits.clear()
                var userVisited = false
                for (touristDataSnapshot in dataSnapshot.children) {
                    if (touristDataSnapshot.hasChild(userId)) {
                        userVisited = true
                        val totalVisits = touristDataSnapshot.child(userId).child("total").getValue(Int::class.java) ?: 0

                        val name = touristDataSnapshot.child(userId).child("name").getValue(String::class.java) ?: ""

                        val tourData = TourData(name, "", "", "", null, null)
                        tourData.name = name
                        tourData.totalVisits = totalVisits
                        touristVisits.add(tourData)
                    }
                }
                // Periksa apakah pengguna telah mengunjungi wisata
                if (!userVisited) {
                    // Tampilkan pesan jika pengguna belum mengunjungi wisata
                    binding.recyclerViewKunjungan.visibility = View.GONE
                    binding.notFound.visibility = View.VISIBLE
                } else {
                    // Tampilkan daftar kunjungan jika pengguna telah mengunjungi wisata
                    binding.recyclerViewKunjungan.visibility = View.VISIBLE
                    binding.notFound.visibility = View.GONE
                    visitsAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

}