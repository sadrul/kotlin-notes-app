package com.example.shoppinglist

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ListDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ItemAdapter
    private val items = mutableListOf<Triple<String, String, Boolean>>() // id, name, status
    private lateinit var listId: String
    private lateinit var listName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_detail)

        listId = intent.getStringExtra("LIST_ID") ?: return
        listName = intent.getStringExtra("LIST_NAME") ?: "Shopping List"

        title = listName

        db = FirebaseFirestore.getInstance()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewItems)
        val btnAddItem = findViewById<Button>(R.id.btnAddItem)

        adapter = ItemAdapter(items,
            onCheck = { id, checked ->
                db.collection("shoppingLists").document(listId)
                    .collection("items").document(id)
                    .update("completed", checked)
            },
            onLongClick = { id ->
                showEditDeleteItemDialog(id)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAddItem.setOnClickListener {
            showAddItemDialog()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        loadItems()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showAddItemDialog() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("Add Item")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val data = mapOf("name" to name, "completed" to false)
                    db.collection("shoppingLists").document(listId)
                        .collection("items").add(data)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDeleteItemDialog(itemId: String) {
        val item = items.find { it.first == itemId } ?: return
        val input = EditText(this)
        input.setText(item.second)
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("Edit or Delete Item")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    db.collection("shoppingLists").document(listId)
                        .collection("items").document(itemId)
                        .update("name", newName)
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                db.collection("shoppingLists").document(listId)
                    .collection("items").document(itemId).delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadItems() {
        db.collection("shoppingLists").document(listId)
            .collection("items")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    items.clear()
                    for (doc in snapshot.documents) {
                        val name = doc.getString("name") ?: continue
                        val completed = doc.getBoolean("completed") ?: false
                        items.add(Triple(doc.id, name, completed))
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}