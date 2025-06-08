package com.example.shoppinglist

import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingAdapter
    private val items = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        val addButton = findViewById<Button>(R.id.addButton)

        adapter = ShoppingAdapter(items) { item -> deleteItem(item) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadItems()

        addButton.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Item")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val item = input.text.toString()
            if (item.isNotEmpty()) {
                addItem(item)
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun addItem(item: String) {
        val itemMap = hashMapOf("name" to item)
        db.collection("shoppingList").add(itemMap)
    }

    private fun deleteItem(item: String) {
        db.collection("shoppingList")
            .whereEqualTo("name", item)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    db.collection("shoppingList").document(doc.id).delete()
                }
            }
    }

    private fun loadItems() {
        db.collection("shoppingList").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                items.clear()
                for (doc in snapshot.documents) {
                    val name = doc.getString("name")
                    if (name != null) items.add(name)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
}