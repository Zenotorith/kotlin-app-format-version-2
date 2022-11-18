package com.example.kotlinnativevincent.models

data class SaveTrip(
    val tripName : String ?= null,
    val destination : String ?= null,
    val date : String ?= null,
    val description : String ?= null,
    val participant : String ?= null,
    val vehicle : String ?= null,
    val riskAssessment : String ?= null,
)
