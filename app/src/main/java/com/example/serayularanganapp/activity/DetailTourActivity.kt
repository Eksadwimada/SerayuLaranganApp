package com.example.serayularanganapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.serayularanganapp.databinding.ActivityDetailTourBinding
import com.example.serayularanganapp.model.TourData
import com.google.firebase.FirebaseApp

class DetailTourActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTourBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize View Binding
        binding = ActivityDetailTourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan data tur dari intent
        val tourData = intent.getSerializableExtra("Wisata") as TourData

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
    }
}