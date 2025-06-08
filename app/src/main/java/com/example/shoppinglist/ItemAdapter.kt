package com.example.shoppinglist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    private val items: List<Triple<String, String, Boolean>>,
    private val onCheck: (String, Boolean) -> Unit,
    private val onLongClick: (String) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemCheck: CheckBox = view.findViewById(R.id.itemCheck)
        init {
            itemCheck.setOnCheckedChangeListener { _, isChecked ->
                val (id, _, _) = items[adapterPosition]
                onCheck(id, isChecked)
            }
            view.setOnLongClickListener {
                val (id, _, _) = items[adapterPosition]
                onLongClick(id)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (_, name, completed) = items[position]
        holder.itemName.text = name
        holder.itemCheck.isChecked = completed
    }
}