package com.example.kotlinnativevincent.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinnativevincent.R
import com.example.kotlinnativevincent.activities.DetailTrip
import com.example.kotlinnativevincent.adapters.ListTripAdapter
import com.example.kotlinnativevincent.databinding.FragmentShowTripListBinding
import com.example.kotlinnativevincent.models.ShowListTrip
import com.example.kotlinnativevincent.models.ShowListTripViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ShowTripList : Fragment() {

    private var _binding: FragmentShowTripListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel : ShowListTripViewModel
    private lateinit var tripRecyclerView: RecyclerView
    private lateinit var adapter: ListTripAdapter
    private val url : String = "https://cw1-vincent-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val database : DatabaseReference = FirebaseDatabase.getInstance(url).getReference("Trips")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentShowTripListBinding.inflate(inflater, container, false)

        //Search general
        binding.searchGeneralView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchGeneralTrip(newText)
                return false
            }
        })

        //Enable Condition Button (Search Dialog)
        binding.enableSearchDialogButton.setOnClickListener {
            val dialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.search_condition_dialog, null)
            val searchDialog =  AlertDialog.Builder(requireContext())
            searchDialog.setView(dialogLayout)
            searchDialog.show()

            val button = dialogLayout.findViewById<Button>(R.id.searchConditionButton)
            val inputSearchDate = dialogLayout.findViewById<TextInputEditText>(R.id.searchDateInput)

            //Date Picker
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                pickDate(calendar, inputSearchDate)
            }

            inputSearchDate.setOnClickListener {
                DatePickerDialog(requireContext(), datePicker, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            //Search Button
            button.setOnClickListener {

                val tripNameParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchTripNameInput).text.toString()
                val destinationParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchDestinationInput).text.toString()
                val dateParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchDateInput).text.toString()

                database.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val filteredList = mutableListOf<ShowListTrip>()
                            val trip : List<ShowListTrip> = snapshot.children.map { dataSnapshot ->

                                dataSnapshot.getValue(ShowListTrip::class.java)!!

                            }

                            searchConditionTrip(tripNameParam, destinationParam, dateParam, filteredList, trip)

                            val filteredTrips: List<ShowListTrip> = filteredList.toList()
                            adapter.updateTripList(filteredTrips)
                        } catch (e: Exception) {

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

            }


        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripRecyclerView = binding.tripListShow
        tripRecyclerView.layoutManager = LinearLayoutManager(context)
        tripRecyclerView.setHasFixedSize(true)
        adapter = ListTripAdapter()
        tripRecyclerView.adapter = adapter

        viewModel = ViewModelProvider(this).get(ShowListTripViewModel::class.java)

        viewModel.allTrips.observe(viewLifecycleOwner, Observer { it ->

            adapter.updateTripList(it)

            adapter.onItemClick = {
                val intent = Intent(requireContext(), DetailTrip::class.java)
                intent.putExtra("trip", it)
                startActivity(intent)
            }
        })
    }

    private fun pickDate(calendar: Calendar, inputSearchDate: TextInputEditText) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        inputSearchDate.setText(sdf.format(calendar.time))
    }

    private fun searchGeneralTrip(queryText: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val filteredList = mutableListOf<ShowListTrip>()
                    val trip : List<ShowListTrip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(ShowListTrip::class.java)!!

                    }

                    for (item in trip) {
                        if (
                            item.tripName!!.lowercase().contains(queryText.lowercase()) ||
                            item.date!!.lowercase().contains(queryText.lowercase()) ||
                            item.destination!!.lowercase().contains(queryText.lowercase())) {
                            filteredList.add(item)
                        }
                    }

                    val filteredTrips: List<ShowListTrip> = filteredList.toList()
                    adapter.updateTripList(filteredTrips)

                }catch (e : Exception){
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun searchConditionTrip(tripNameSearchValue: String, destinationSearchValue: String,
                                dateSearchValue: String, filteredList: MutableList<ShowListTrip>, trip: List<ShowListTrip>) {
        if(tripNameSearchValue.isNotEmpty() && destinationSearchValue.isEmpty() && dateSearchValue.isEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameSearchValue.lowercase())) {
                    filteredList.add(item)
                }
            }
        }

        if(tripNameSearchValue.isEmpty() && destinationSearchValue.isNotEmpty() && dateSearchValue.isEmpty()) {
            for (item in trip) {
                if (
                    item.destination!!.lowercase().contains(destinationSearchValue.lowercase())) {
                    filteredList.add(item)
                }
            }
        }

        if(tripNameSearchValue.isEmpty() && destinationSearchValue.isEmpty() && dateSearchValue.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.date!!.lowercase().contains(dateSearchValue.lowercase())) {
                    filteredList.add(item)
                }
            }
        }

        if(tripNameSearchValue.isNotEmpty() && destinationSearchValue.isNotEmpty() && dateSearchValue.isEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameSearchValue.lowercase()) &&
                    item.destination!!.lowercase().contains(destinationSearchValue.lowercase())) {
                    filteredList.add(item)
                }
            }
        }

        if(tripNameSearchValue.isNotEmpty() && destinationSearchValue.isEmpty() && dateSearchValue.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameSearchValue.lowercase()) &&
                    item.date!!.lowercase().contains(dateSearchValue.lowercase())) {
                    filteredList.add(item)
                }
            }
        }

        if(tripNameSearchValue.isEmpty() && destinationSearchValue.isNotEmpty() && dateSearchValue.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.destination!!.lowercase().contains(destinationSearchValue.lowercase()) &&
                    item.date!!.lowercase().contains(dateSearchValue.lowercase())) {
                    filteredList.add(item)
                }
            }
        }

        if(tripNameSearchValue.isNotEmpty() && destinationSearchValue.isNotEmpty() && dateSearchValue.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameSearchValue.lowercase()) &&
                    item.destination!!.lowercase().contains(destinationSearchValue.lowercase()) &&
                    item.date!!.lowercase().contains(dateSearchValue.lowercase())) {
                    filteredList.add(item)
                }
            }
        }

        if(tripNameSearchValue.isEmpty() && destinationSearchValue.isEmpty() && dateSearchValue.isEmpty()) {
            for (item in trip) {
                filteredList.add(item)
            }
        }

    }


}