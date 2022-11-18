package com.example.kotlinnativevincent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinnativevincent.R
import com.example.kotlinnativevincent.models.SaveExpense

class ListExpenseAdapter: RecyclerView.Adapter<ListExpenseAdapter.ExpenseViewHolder>() {

    private val expenseList = ArrayList<SaveExpense>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.expense_item,
            parent,false
        )
        return ExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentItem = expenseList[position]

        holder.expenseType.text = currentItem.amount
        holder.amount.text = currentItem.expenseType
        holder.date.text = currentItem.date
        holder.time.text = currentItem.time
        holder.comment.text = currentItem.comment

    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    fun updateExpenseList(expenseList : List<SaveExpense>){
        this.expenseList.clear()
        this.expenseList.addAll(expenseList)
        notifyDataSetChanged()

    }

    class ExpenseViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val expenseType : TextView = itemView.findViewById(R.id.expenseTypeItem)
        val amount : TextView = itemView.findViewById(R.id.amountItem)
        val date : TextView = itemView.findViewById(R.id.dateItem)
        val time : TextView = itemView.findViewById(R.id.timeItem)
        val comment : TextView = itemView.findViewById(R.id.commentItem)

    }
}