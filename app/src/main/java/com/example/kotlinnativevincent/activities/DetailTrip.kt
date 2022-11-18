package com.example.kotlinnativevincent.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinnativevincent.adapters.ListExpenseAdapter
import com.example.kotlinnativevincent.databinding.ActivityDetailTripBinding
import com.example.kotlinnativevincent.models.ShowListTrip
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import com.example.kotlinnativevincent.R
import com.example.kotlinnativevincent.models.SaveExpense

class DetailTrip : AppCompatActivity() {

    private lateinit var binding : ActivityDetailTripBinding

    private val url : String = "https://cw1-vincent-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val database : DatabaseReference = FirebaseDatabase.getInstance(url).getReference("Trips")

    private lateinit var tripNameDetailValue : String
    private lateinit var destinationDetailValue : String
    private lateinit var dateDetailValue : String
    private lateinit var riskAssessmentDetailValue : String
    private lateinit var descriptionDetailValue : String
    private lateinit var participantDetailValue : String
    private lateinit var vehicleDetailValue : String


    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var adapter: ListExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Pass data into detail activity
        val trip = intent.getParcelableExtra<ShowListTrip>("trip")
        if(trip?.tripName!!.isNotEmpty()) {

            tripNameDetailValue = trip.tripName

            readTripData(
                trip.tripName,
                binding.tripNameDetail,
                binding.destinationDetail,
                binding.dateDetail,
                binding.riskAssessmentDetail,
                binding.descriptionDetail,
                binding.participantDetail,
                binding.vehicleDetail)

            showExpenses(LinearLayoutManager(this))
        }

        //Update Dialog
        val updateButton: MaterialButton = binding.updateButton

        updateButton.setOnClickListener {
            updateTrip()
        }

        //Delete Dialog
        val deleteButton: MaterialButton = binding.deleteButton
        deleteButton.setOnClickListener {
            deleteTrip()
        }

        //Add Expense Dialog
        val addButton : MaterialButton = binding.addExpenseButton
        addButton.setOnClickListener {
            addExpense()
        }

    }

    private fun readTripData(tripName: String, tripNameView: TextView, destinationView: TextView,
                             dateView: TextView, riskAssessmentView: TextView, descriptionView: TextView,
                             participantView: TextView, vehicleView: TextView
    ) {
        database.child(tripName).get().addOnSuccessListener {

            if (it.exists()){

                destinationDetailValue = it.child("destination").value.toString()
                dateDetailValue = it.child("date").value.toString()
                riskAssessmentDetailValue = it.child("riskAssessment").value.toString()
                descriptionDetailValue = it.child("description").value.toString()
                participantDetailValue = it.child("participant").value.toString()
                vehicleDetailValue = it.child("vehicle").value.toString()

                tripNameView.text = tripNameDetailValue
                destinationView.text = destinationDetailValue
                dateView.text = dateDetailValue
                riskAssessmentView.text = riskAssessmentDetailValue
                descriptionView.text = descriptionDetailValue
                participantView.text = participantDetailValue
                vehicleView.text = vehicleDetailValue
            }
        }

    }

    private fun deleteTrip() {
        val message = "Confirm to delete this trip."
        AlertDialog.Builder(this)
            .setTitle("Delete Trip")
            .setMessage(message)
            .setPositiveButton("Confirm"){ _,_ ->
                database.child(tripNameDetailValue).removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Successfully Deleted", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateTrip() {
        val updateDialog = LayoutInflater.from(this).inflate(R.layout.update_trip, null)

        //Date Picker
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            pickDate(calendar, updateDialog.findViewById(R.id.dateUpdate))
        }

        updateDialog.findViewById<TextInputEditText>(R.id.dateUpdate).setOnClickListener {
            DatePickerDialog(this, datePicker, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val tripNameBase : TextInputLayout = updateDialog.findViewById(R.id.tripNameBase)
        val destinationBase : TextInputLayout = updateDialog.findViewById(R.id.destinationBase)
        val dateBase : TextInputLayout = updateDialog.findViewById(R.id.dateBase)
        val riskAssessment : SwitchCompat = updateDialog.findViewById(R.id.assessmentUpdate)
        val descriptionBase : TextInputLayout = updateDialog.findViewById(R.id.descriptionBase)
        val participantBase : TextInputLayout = updateDialog.findViewById(R.id.participantBase)
        val vehicleBase : TextInputLayout = updateDialog.findViewById(R.id.vehicleBase)

        setTextInput(tripNameBase, destinationBase, dateBase, riskAssessment, descriptionBase, participantBase, vehicleBase)


        riskAssessment.setOnCheckedChangeListener { _, isChecked ->
            val value = if(isChecked)
                "Yes"
            else
                "No"
            if(riskAssessmentDetailValue != value) {
                riskAssessmentDetailValue = value
            }
        }

        //Create dialog
        val searchDialog = AlertDialog.Builder(this)
        searchDialog.setTitle("Update Trip")
        searchDialog.setView(updateDialog)
        searchDialog.setPositiveButton("Update") { _, _ ->
            val destination : TextInputEditText = updateDialog.findViewById(R.id.destinationUpdate)
            val date : TextInputEditText = updateDialog.findViewById(R.id.dateUpdate)
            val description : TextInputEditText = updateDialog.findViewById(R.id.descriptionUpdate)
            val participant : TextInputEditText = updateDialog.findViewById(R.id.participantUpdate)
            val vehicle : TextInputEditText = updateDialog.findViewById(R.id.vehicleUpdate)

            val newTripUpdate = mapOf(
                "destination" to destination.text.toString(),
                "date" to date.text.toString(),
                "description" to description.text.toString(),
                "participant" to participant.text.toString(),
                "vehicle" to vehicle.text.toString(),
                "riskAssessment" to riskAssessmentDetailValue
            )

            database.child(tripNameDetailValue).updateChildren(newTripUpdate).addOnSuccessListener {
                Toast.makeText(this, "Successfully Updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
        searchDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()

        }
        searchDialog.show()
    }

    private fun setTextInput(tripNameView: TextInputLayout, destinationView: TextInputLayout,
                             dateView: TextInputLayout, riskAssessmentView: SwitchCompat, descriptionView: TextInputLayout,
                             participantView: TextInputLayout, vehicleView: TextInputLayout
    ) {

        tripNameView.editText?.setText(tripNameDetailValue)
        destinationView.editText?.setText(destinationDetailValue)
        dateView.editText?.setText(dateDetailValue)
        descriptionView.editText?.setText(descriptionDetailValue)
        participantView.editText?.setText(participantDetailValue)
        vehicleView.editText?.setText(vehicleDetailValue)

        if(riskAssessmentDetailValue == "Yes") {
            riskAssessmentView.isChecked = true
        }

    }

    private fun checkValidation(expenseTypeView: AutoCompleteTextView,
                                   amountView: TextInputEditText, dateView: TextInputEditText, timeView: TextInputEditText, dialog: View
    ) {

        //Expense Type
        expenseTypeView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.expenseTypeBase).helperText = validExpenseType(dialog)
            }
        }

        //Amount
        amountView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.amountBase).helperText = validAmount(dialog)
            }
        }

        //Date
        dateView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.dateBase).helperText = validDate(dialog)
            }
        }

        //Time
        timeView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.timeBase).helperText = validTime(dialog)
            }
        }

    }

    private fun validExpenseType(dialog: View): String? {
        val expenseType = dialog.findViewById<AutoCompleteTextView>(R.id.expenseTypeInput).text.toString()
        if(expenseType.isEmpty()) {
            return "Required Field"
        }
        return null
    }

    private fun validAmount(dialog: View): String? {
        val amount = dialog.findViewById<TextInputEditText>(R.id.amountInput).text.toString()
        if(amount.isEmpty()) {
            return "Required Field"
        }
        return null
    }

    private fun validDate(dialog: View): String? {
        val date = dialog.findViewById<TextInputEditText>(R.id.dateInput).text.toString()
        if(date.isEmpty()) {
            return "Required Field"
        }
        return null
    }

    private fun validTime(dialog: View): String? {
        val time = dialog.findViewById<TextInputEditText>(R.id.timeInput).text.toString()
        if(time.isEmpty()) {
            return "Required Field"
        }
        return null
    }

    private fun submitForm(dialog: View)
    {
        val expenseTypeView : TextInputLayout = dialog.findViewById(R.id.expenseTypeBase)
        val amountView : TextInputLayout = dialog.findViewById(R.id.amountBase)
        val dateView : TextInputLayout = dialog.findViewById(R.id.dateBase)
        val timeView : TextInputLayout = dialog.findViewById(R.id.timeBase)

        expenseTypeView.helperText = validExpenseType(dialog)
        amountView.helperText = validAmount(dialog)
        dateView.helperText = validDate(dialog)
        timeView.helperText = validTime(dialog)

        val validExpenseType = expenseTypeView.helperText === null
        val validAmount = amountView.helperText === null
        val validDate = dateView.helperText === null
        val validTime = timeView.helperText === null

        if (validExpenseType && validAmount && validDate && validTime) {
            return saveData(
                dialog.findViewById<AutoCompleteTextView>(R.id.expenseTypeInput).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.amountInput).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.dateInput).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.timeInput).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.commentInput).text.toString(),
                dialog)
        }
        return

    }

    private fun saveData(
        expenseType: String,
        amount: String,
        date: String,
        time: String,
        comment: String,
        dialog: View
    ) {
        val newExpense = SaveExpense(
            expenseType,
            amount,
            date,
            time,
            comment
        )
        database.child(tripNameDetailValue).child("expenses").push().setValue(newExpense).addOnSuccessListener {
            dialog.findViewById<AutoCompleteTextView>(R.id.expenseTypeInput).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.amountInput).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.dateInput).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.timeInput).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.commentInput).text?.clear()

            Toast.makeText(this, "Successfully Added", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addExpense() {
        val addDialog = LayoutInflater.from(this).inflate(R.layout.add_expense, null)

        //Date Picker
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            pickDate(calendar, addDialog.findViewById(R.id.dateInput))
        }

        addDialog.findViewById<TextInputEditText>(R.id.dateInput).setOnClickListener {
            DatePickerDialog(this, datePicker, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //Time Picker
        val timeInput : TextInputEditText = addDialog.findViewById(R.id.timeInput)
        timeInput.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val startHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val startMinute = currentTime.get(Calendar.MINUTE)

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                timeInput.setText("$hourOfDay:$minute")
            }, startHour, startMinute, false).show()

        }

        //Dropdown Menu
        val items = listOf("Travel", "Food", "Other")
        val adapter = ArrayAdapter(this, R.layout.select_expense_type ,items)
        addDialog.findViewById<AutoCompleteTextView>(R.id.expenseTypeInput).setAdapter(adapter)

        //Create dialog
        val addExpenseDialog = AlertDialog.Builder(this)
        addExpenseDialog.setView(addDialog)
        addExpenseDialog.show()

        //Validate
        checkValidation(
            addDialog.findViewById(R.id.expenseTypeInput),
            addDialog.findViewById(R.id.amountInput),
            addDialog.findViewById(R.id.dateInput),
            addDialog.findViewById(R.id.timeInput),
            addDialog
        )

        addDialog.findViewById<MaterialButton>(R.id.submitExpenseButton).setOnClickListener {
            submitForm(addDialog)
        }

    }

    private fun pickDate(myCalendar: Calendar, inputText: TextInputEditText) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        inputText.setText(sdf.format(myCalendar.time))
    }

    private fun showExpenses(layout: LinearLayoutManager) {
        database.child(tripNameDetailValue).child("expenses").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {

                    val expenseList : List<SaveExpense> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(SaveExpense::class.java)!!

                    }

                    expenseRecyclerView = binding.listExpense
                    expenseRecyclerView.layoutManager = layout
                    expenseRecyclerView.setHasFixedSize(true)
                    adapter = ListExpenseAdapter()
                    expenseRecyclerView.adapter = adapter

                    adapter.updateExpenseList(expenseList)

                }catch (e : Exception){
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}