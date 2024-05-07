package com.example.serayularanganapp.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.serayularanganapp.databinding.ActivityDetailTourBinding
import com.example.serayularanganapp.model.TourData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailTourActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTourBinding
    private lateinit var mapView: MapView
    private lateinit var tourData: TourData
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize View Binding
        binding = ActivityDetailTourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan data tur dari intent
        tourData = intent.getSerializableExtra("Wisata") as TourData

        // Set data to views
        binding.tvNamaWisata.text = tourData.name
        binding.tvDeskripsi.text = tourData.desc
        binding.tvInfoWisata.text = tourData.info

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

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            val tourLocation = LatLng(tourData.latitude ?: 0.0, tourData.longitude ?: 0.0)
            val markerOptions = MarkerOptions().position(tourLocation).title(tourData.name)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Set warna marker merah
            googleMap.addMarker(markerOptions)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tourLocation, 15f))

            googleMap.setOnMapClickListener { latLng ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=${tourLocation.latitude},${tourLocation.longitude}")
                )
                startActivity(intent)
                Log.d("DetailTourActivity", "Opening Google Maps Navigation")
            }
        }

        //mengambil userId saat ini
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Update visitor counts
        updateVisitorCounts(tourData)
    }

    private fun updateVisitorCounts(tourData: TourData) {
        val wisataName = tourData.name ?: ""

        val currentDateString = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

        // ref untuk jumlah pengunjung hari ini
        val jumlahHariIniRef = FirebaseDatabase.getInstance().getReference("PengunjungHariIni").child(wisataName).child(currentDateString)


        // get jumlah pengunjung hari ini
        jumlahHariIniRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val hariIni = snapshot.child("HariIni").getValue(Int::class.java) ?: 0
                    binding.tvJumlahPengunjung.text = "${hariIni.toString()} Pengunjung"
                } else {
                    binding.tvJumlahPengunjung.text = "0 Pengunjung"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
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
