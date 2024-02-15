package com.example.serayularanganapp.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serayularanganapp.activity.DetailTourActivity
import com.example.serayularanganapp.adapter.TourAdapter
import com.example.serayularanganapp.databinding.FragmentHomeBinding
import com.example.serayularanganapp.model.TourData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private lateinit var tourDataRef: DatabaseReference

    // Daftar tour asli sebelum pencarian
    private var originalTourDataList: List<TourData> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        tourDataRef = database.getReference("Wisata")

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager


        val adapter = TourAdapter(requireContext(), emptyList()) { tourData ->
            val intent = Intent(requireContext(), DetailTourActivity::class.java)
            intent.putExtra("Wisata", tourData)
            startActivity(intent)
        }

        // Mengatur adapter pada RecyclerView
        binding.recyclerView.adapter = adapter

        // perubahan data pada Firebase Database
        tourDataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tourDataList = mutableListOf<TourData>()

                for (dataSnapshot in snapshot.children) {
                    val tourData = dataSnapshot.getValue(TourData::class.java)
                    tourData?.let {
                        tourDataList.add(it)
                    }
                }

                originalTourDataList = tourDataList.toList()
                adapter.setData(tourDataList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })

        // Set listener for searchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchTourData(it) }
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            val inputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            false
        }
    }

    // Fungsi untuk melakukan pencarian data tur berdasarkan nama
    private fun searchTourData(query: String) {
        val searchResult = originalTourDataList.filter {
            it.name?.contains(query, ignoreCase = true) == true
        }
        (binding.recyclerView.adapter as TourAdapter).setData(searchResult)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
