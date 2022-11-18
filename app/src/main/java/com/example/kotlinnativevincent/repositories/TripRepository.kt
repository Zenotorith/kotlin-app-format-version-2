package com.example.kotlinnativevincent.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.kotlinnativevincent.models.ShowListTrip
import com.google.firebase.database.*
import java.lang.Exception

class TripRepository {
    private val url : String = "https://cw1-vincent-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val databaseReference : DatabaseReference = FirebaseDatabase.getInstance(url).getReference("Trips")

    @Volatile private var INSTANCE : TripRepository ?= null

    fun getInstance() : TripRepository{
        return INSTANCE ?: synchronized(this){

            val instance = TripRepository()
            INSTANCE = instance
            instance
        }

    }

    fun loadTrips(tripList : MutableLiveData<List<ShowListTrip>>){

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                try {

                    val tripListResult : List<ShowListTrip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(ShowListTrip::class.java)!!

                    }

                    tripList.postValue(tripListResult)

                }catch (e : Exception){ }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}