package com.example.serayularanganapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.serayularanganapp.R
import com.example.serayularanganapp.model.TourData

class TourAdapter(
    private val context: Context,
    private var dataList: List<TourData>,
    private val itemClickListener: (TourData) -> Unit
) : RecyclerView.Adapter<TourAdapter.TourViewHolder>() {

    fun setData(newData: List<TourData>) {
        dataList = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_tour, parent, false)
        return TourViewHolder(view)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        Glide.with(context).load(dataList[position].img).into(holder.recImage)
        holder.recName.text = dataList[position].name

        //click
        holder.itemView.setOnClickListener {
            itemClickListener.invoke(dataList[position])
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class TourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recImage: ImageView = itemView.findViewById(R.id.imageWisata)
        var recName: TextView = itemView.findViewById(R.id.tvNamaWisata)
    }
}
