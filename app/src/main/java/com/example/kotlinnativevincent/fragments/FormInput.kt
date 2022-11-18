package com.example.kotlinnativevincent.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.example.kotlinnativevincent.R
import com.example.kotlinnativevincent.databinding.FragmentFormInputBinding
import com.example.kotlinnativevincent.models.SaveTrip
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class FormInput : Fragment() {

    private var _binding: FragmentFormInputBinding? = null
    private val binding get() = _binding!!

    private val url : String = "https://cw1-vincent-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val database : DatabaseReference = FirebaseDatabase.getInstance(url).getReference("Trips")

    //Input Text
    private lateinit var tripNameInput : TextInputEditText
    private lateinit var destinationInput : TextInputEditText
    private lateinit var dateInput : TextInputEditText
    private lateinit var descriptionInput : TextInputEditText
    private lateinit var participantInput : TextInputEditText
    private lateinit var vehicleInput : AutoCompleteTextView

    //Input Base
    private lateinit var tripNameBase : TextInputLayout
    private lateinit var destinationBase : TextInputLayout
    private lateinit var dateBase : TextInputLayout
    private lateinit var descriptionBase : TextInputLayout
    private lateinit var participantBase : TextInputLayout
    private lateinit var vehicleBase : TextInputLayout

    private lateinit var riskAssessmentSwitch : SwitchCompat
    private var riskAssessmentValue : String = "No"
    private lateinit var submitTripButton : MaterialButton

    private lateinit var tripNameListValidation : List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFormInputBinding.inflate(inflater, container, false)

        //Set binding
        tripNameInput = binding.tripNameInput
        destinationInput = binding.destinationInput
        dateInput = binding.dateInput
        descriptionInput = binding.descriptionInput
        participantInput = binding.participantInput
        vehicleInput = binding.vehicleInput
        tripNameBase = binding.tripNameBase
        destinationBase = binding.destinationBase
        dateBase = binding.dateBase
        descriptionBase = binding.descriptionBase
        participantBase = binding.participantBase
        vehicleBase = binding.vehicleBase
        riskAssessmentSwitch = binding.riskAssessment
        submitTripButton = binding.submitTripButton

        //Make trip list for validation
        getTripList()

        //Date Picker
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            pickDate(calendar)
        }

        dateInput.setOnClickListener {
            DatePickerDialog(requireContext(), datePicker, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //Select Vehicle
        val vehicleItems = listOf("Plane", "Train", "Car", "Bus", "MotorBike", "Boat")
        val adapter = ArrayAdapter(requireContext(), R.layout.select_vehicles , vehicleItems)
        vehicleInput.setAdapter(adapter)

        //Validate
        checkValidation()
        submitTripButton.setOnClickListener {
            submitTripFormInput()
        }

        riskAssessmentSwitch.setOnCheckedChangeListener { _, isChecked ->
            val value = if (isChecked)
                "Yes"
            else
                "No"
            riskAssessmentValue = value
        }

        return binding.root
    }

    private fun pickDate(calendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        dateInput.setText(sdf.format(calendar.time))
    }

    private fun submitTripFormInput()
    {
        tripNameBase.helperText = validTripName()
        destinationBase.helperText = validDestination()
        dateBase.helperText = validDate()


        val validTripName = tripNameBase.helperText === null
        val validDestination = destinationBase.helperText === null
        val validDate = dateBase.helperText === null

        if (validTripName && validDestination && validDate) {
            return handleSubmitTripForm()
        }
        return
    }

    private fun handleSubmitTripForm()
    {
        var body = "Trip Name: ${tripNameInput.text}"
        body += "\nDestination: ${destinationInput.text}"
        body += "\nDate: ${dateInput.text}"
        body += "\nDescription: ${descriptionInput.text}"
        body += "\nParticipant: ${participantInput.text}"
        body += "\nVehicle: ${vehicleInput.text}"
        body += "\nRisk Assessment: $riskAssessmentValue"
        AlertDialog.Builder(requireContext())
            .setMessage(body)
            .setPositiveButton("Confirm"){ _,_ ->
                storeOnFirebase(
                    tripNameInput.text.toString(),
                    destinationInput.text.toString(),
                    dateInput.text.toString(),
                    descriptionInput.text.toString(),
                    participantInput.text.toString(),
                    vehicleInput.text.toString(),
                    riskAssessmentValue,
                )
            }
            .setNegativeButton("Edit") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun storeOnFirebase(
        tripNameValue: String,
        destinationValue: String,
        dateValue: String,
        descriptionValue: String,
        participantValue: String,
        vehicleValue: String,
        riskAssessment: String,
    ) {
        val newTrip = SaveTrip(
            tripNameValue,
            destinationValue,
            dateValue,
            descriptionValue,
            participantValue,
            vehicleValue,
            riskAssessment,
        )
        database.child(tripNameValue).setValue(newTrip).addOnSuccessListener {
            tripNameInput.text?.clear()
            destinationInput.text?.clear()
            dateInput.text?.clear()
            descriptionInput.text?.clear()
            participantInput.text?.clear()
            vehicleInput.text?.clear()
            Toast.makeText(requireContext(), "Successfully Submit Form", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed Submit", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTripList() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val itemList = mutableListOf<String>()
                    val trip : List<SaveTrip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(SaveTrip::class.java)!!
                    }

                    for (item in trip) {
                        itemList.add(item.tripName!!)
                    }

                    tripNameListValidation = itemList.toList()

                } catch (e: Exception) {
                    Log.d("Error: ", e.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Query Error: ", error.toString())
            }

        })

    }

    private fun checkValidation() {

        //Trip Name
        tripNameInput.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                tripNameBase.helperText = validTripName()
            }
        }

        //Destination
        destinationInput.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                destinationBase.helperText = validDestination()
            }
        }

        //Date
        dateInput.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dateBase.helperText = validDate()
            }
        }
    }

    private fun validTripName(): String? {
        val tripNameValue = tripNameInput.text.toString()
        if(tripNameValue.isEmpty()) {
            return "Required Field"
        }

        if(tripNameValue.trim() in tripNameListValidation) {
            return "Trip Name already exist"
        }

        return null
    }


    private fun validDestination(): String? {
        val destinationValue = destinationInput.text.toString()
        if(destinationValue.isEmpty()) {
            return "Required Field"
        }
        return null
    }

    private fun validDate(): String? {
        val dateValue = dateInput.text.toString()
        if(dateValue.isEmpty()) {
            return "Required Field"
        }
        return null
    }
}