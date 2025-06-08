package com.example.shoppinglist

import android.content.Intent
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
    private lateinit var listRecyclerView: RecyclerView
    private lateinit var adapter: ShoppingListAdapter
    private val shoppingLists = ArrayList<Pair<String, String>>() // Pair<docId, name>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()
        listRecyclerView = findViewById(R.id.recyclerViewLists)
        val addListBtn = findViewById<Button>(R.id.btnAddList)

        adapter = ShoppingListAdapter(shoppingLists,
            onClick = { docId, name ->
                val intent = Intent(this, ListDetailActivity::class.java)
                intent.putExtra("LIST_ID", docId)
                intent.putExtra("LIST_NAME", name)
                startActivity(intent)
            },
            onLongClick = { docId ->
                showEditDeleteDialog(docId)
            }
        )

        listRecyclerView.layoutManager = LinearLayoutManager(this)
        listRecyclerView.adapter = adapter

        addListBtn.setOnClickListener { showAddListDialog() }

        loadLists()
    }

    private fun showAddListDialog() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("New List Name")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    db.collection("shoppingLists").add(mapOf("name" to name))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDeleteDialog(docId: String) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        val currentList = shoppingLists.find { it.first == docId }
        input.setText(currentList?.second)

        AlertDialog.Builder(this)
            .setTitle("Edit or Delete List")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    db.collection("shoppingLists").document(docId)
                        .update("name", newName)
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                db.collection("shoppingLists").document(docId).delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadLists() {
        db.collection("shoppingLists").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                shoppingLists.clear()
                for (doc in snapshot.documents) {
                    val name = doc.getString("name") ?: continue
                    shoppingLists.add(doc.id to name)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
}