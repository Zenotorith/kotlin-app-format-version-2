package com.example.kotlinnativevincent.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinnativevincent.R
import com.example.kotlinnativevincent.models.ShowListTrip

class ListTripAdapter: RecyclerView.Adapter<ListTripAdapter.MyViewHolder>() {

    private val tripListArray = ArrayList<ShowListTrip>()
    var onItemClick: ((ShowListTrip) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.trip_item,
            parent,false
        )
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("Test Binding", tripListArray.toString())
        val currentItem = tripListArray[position]

        holder.tripName.text = currentItem.tripName
        holder.destination.text = currentItem.destination
        holder.date.text = currentItem.date

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(currentItem)
        }
    }

    override fun getItemCount(): Int {
        Log.d("Test Size", tripListArray.size.toString())
        return tripListArray.size
    }

    fun updateTripList(tripList : List<ShowListTrip>){
        Log.d("Test Before", tripListArray.toString())
        this.tripListArray.clear()
        this.tripListArray.addAll(tripList)
        Log.d("Test After", tripListArray.toString())
        notifyDataSetChanged()

    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val tripName : TextView = itemView.findViewById(R.id.tripNameItem)
        val destination : TextView = itemView.findViewById(R.id.destinationItem)
        val date : TextView = itemView.findViewById(R.id.dateItem)

    }
}