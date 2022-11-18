package com.example.kotlinnativevincent.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinnativevincent.repositories.TripRepository

class ShowListTripViewModel: ViewModel() {
    private val repository : TripRepository
    private val _allTrips = MutableLiveData<List<ShowListTrip>>()
    val allTrips : LiveData<List<ShowListTrip>> = _allTrips


    init {
        repository = TripRepository().getInstance()
        repository.loadTrips(_allTrips)
    }
}