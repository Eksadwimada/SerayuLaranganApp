package com.example.serayularanganapp.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.serayularanganapp.databinding.ActivityDetailTourBinding
import com.example.serayularanganapp.model.TourData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailTourActivity : AppCompatActivity(){

    private lateinit var binding: ActivityDetailTourBinding
    private lateinit var today: String
    private lateinit var mapView: MapView
    private lateinit var tourData: TourData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize View Binding
        binding = ActivityDetailTourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //mengambil data hari ini
        today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        // Mendapatkan data tur dari intent
        tourData = intent.getSerializableExtra("Wisata") as TourData

        // Set data to views
        binding.tvNamaWisata.text = tourData.name
        binding.tvDeskripsi.text = tourData.desc
        binding.tvInfoWisata.text = tourData.info
        binding.tvJumlahPengunjung.text = "${tourData.dailyVisitorCounts[today] ?: 0} Pengunjung"
        binding.tvPengunjungWisata.text = "${tourData.totalVisitor} Total Pengunjung"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        binding.tvToolbarWisata.text = tourData.name

        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle click on the Up button
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Memuat gambar tur menggunakan Glide
        Glide.with(this)
            .load(tourData.img)
            .into(binding.imageWisata)

        //maps
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            // Set up the map with the latitude and longitude from TourData
            val tourLocation = LatLng(tourData.latitude ?: 0.0, tourData.longitude ?: 0.0)
            googleMap.addMarker(MarkerOptions().position(tourLocation).title(tourData.name))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tourLocation, 15f))

            googleMap.setOnMapClickListener { latLng ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=${latLng.latitude},${latLng.longitude}")
                )
                startActivity(intent)
            }
        }

        updateVisitorCounts(tourData)
    }

    private fun updateVisitorCounts(tourData: TourData) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Wisata")

        val updatedTodayVisitors = (tourData.dailyVisitorCounts[today] ?: 0) + 1

        val updatedTotalVisitors = tourData.totalVisitor + 1

        val updateMap = mapOf("dailyVisitorCounts/$today" to updatedTodayVisitors, "totalVisitors" to updatedTotalVisitors)

        //update firebase dengan jumlah pengunjung baru
        databaseReference.child(tourData.name!!).updateChildren(updateMap)
            .addOnSuccessListener {
                fetchAndSetVisitorCounts(tourData)
            }
            .addOnFailureListener { e ->
                // handle the error
                Toast.makeText(this, "Gagal memperbarui: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAndSetVisitorCounts(tourData: TourData) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Wisata")
        databaseReference.child(tourData.name!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val updatedTourData = snapshot.getValue(TourData::class.java)
                    if (updatedTourData != null) {
                        //Atur update jumlah pengunjung ke text
                        binding.tvJumlahPengunjung.text = "${updatedTourData.dailyVisitorCounts[today] ?: 0} Pengunjung"
                        binding.tvPengunjungWisata.text = "${updatedTourData.totalVisitor} Total Pengunjung"
                    } else {
                        Toast.makeText(this@DetailTourActivity, "Data wisata tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Toast.makeText(this@DetailTourActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}