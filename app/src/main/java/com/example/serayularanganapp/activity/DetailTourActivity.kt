package com.example.serayularanganapp.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.serayularanganapp.databinding.ActivityDetailTourBinding
import com.example.serayularanganapp.model.TourData
import com.example.serayularanganapp.model.VisitorData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailTourActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTourBinding
    private lateinit var mapView: MapView
    private lateinit var tourData: TourData

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

        // Memanggil fungsi untuk mengupdate jumlah pengunjung
        updateVisitorCounts(tourData)
    }

    private fun updateVisitorCounts(tourData: TourData) {
        // Mengambil referensi database Firebase
        val databaseReference =
            FirebaseDatabase.getInstance().getReference("Wisata").child(tourData.id ?: "")

        // Mendapatkan data jumlah pengunjung dari Firebase
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Mengambil data jumlah pengunjung dari Firebase
                    val visitorData = snapshot.getValue(VisitorData::class.java)

                    // Memperbarui data jumlah pengunjung di UI
                    visitorData?.let {
                        binding.tvJumlahPengunjung.text = it.visitorCount.toString()
                        binding.tvPengunjungWisata.text = it.totalVisitor.toString()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani kesalahan
                // Misalnya, tampilkan pesan kesalahan
                // Toast.makeText(this@DetailTourActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
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

    }
}