package com.example.serayularanganapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.serayularanganapp.R
import com.example.serayularanganapp.model.TourData

class VisitsAdapter(private val touristVisits: List<TourData>) : RecyclerView.Adapter<VisitsAdapter.TouristVisitsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TouristVisitsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kunjungan, parent, false)
        return TouristVisitsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TouristVisitsViewHolder, position: Int) {
        val touristVisit = touristVisits[position]
        holder.bind(touristVisit)
    }

    override fun getItemCount(): Int {
        return touristVisits.size
    }

    inner class TouristVisitsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTourName: TextView = itemView.findViewById(R.id.tvKunjunganWisata)
        private val textViewTotalVisits: TextView = itemView.findViewById(R.id.tvJumlahKunjungan)

        fun bind(tourData: TourData) {

            val typeface = ResourcesCompat.getFont(itemView.context, R.font.poppinsregular)
            textViewTotalVisits.typeface = typeface

            textViewTourName.text = tourData.name
            textViewTotalVisits.text = "${tourData.totalVisits}  Kali mengunjungi wisata"
        }
    }
}