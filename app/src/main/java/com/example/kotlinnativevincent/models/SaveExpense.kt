package com.example.kotlinnativevincent.models

data class SaveExpense(
    val amount : String ?= null,
    val expenseType : String ?= null,
    val date : String ?= null,
    val time : String ?=  null,
    val comment : String ?= null
)
