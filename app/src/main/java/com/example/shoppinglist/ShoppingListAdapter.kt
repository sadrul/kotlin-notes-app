package com.example.shoppinglist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShoppingListAdapter(
    private val lists: List<Pair<String, String>>,
    private val onClick: (String, String) -> Unit,
    private val onLongClick: (String) -> Unit
) : RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listName: TextView = view.findViewById(R.id.listName)
        init {
            view.setOnClickListener {
                val (id, name) = lists[adapterPosition]
                onClick(id, name)
            }
            view.setOnLongClickListener {
                val (id, _) = lists[adapterPosition]
                onLongClick(id)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lists.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.listName.text = lists[position].second
    }
}