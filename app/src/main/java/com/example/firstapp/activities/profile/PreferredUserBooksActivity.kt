package com.example.firstapp.activities.profile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.activities.MainActivity
import com.example.firstapp.activities.SignupActivity
import com.example.firstapp.activities.items.CardWatchActivity
import com.example.firstapp.databinding.ActivityPreferedUserBooksBinding
import com.example.firstapp.models.Book
import com.example.firstapp.models.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PreferredUserBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferedUserBooksBinding
    private lateinit var firebaseAuth: FirebaseAuth

    val bookList = mutableListOf<Book>()
    val bookNamesList = mutableListOf<String>()
    val userBookNamesList = mutableListOf<String>()
    val searchBookNameList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreferedUserBooksBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updatedUser = firebaseAuth.currentUser
                    if (updatedUser == null) {
                        startActivity(Intent(this, SignupActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, SignupActivity::class.java))
                }
            }
        } else {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.mainPageButton.setOnClickListener {
            startActivity(Intent(this@PreferredUserBooksActivity, MainActivity::class.java))
        }

        val dbBooksRef = FirebaseDatabase.getInstance().getReference("books")

        val dbRef = FirebaseDatabase.getInstance().getReference("prefs/${currentUser!!.uid}")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val dataSnapshotValue = snapshot.value
                    if (dataSnapshotValue is List<*>) {
                        for (item in dataSnapshotValue) {
                            if (item is String) {
                                userBookNamesList.add(item)
                            }
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибки чтения из базы данных
            }
        })



        dbBooksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    if (snapshot.exists()) {
                        val bookMap = snapshot.value as? Map<*, *>
                        if (bookMap != null) {
                            val name = bookMap["name"] as? String
                            val author = bookMap["author"] as? String
                            val tags = bookMap["tags"] as? List<*>

                            if (name != null && author != null && tags != null) {
                                val tagList = tags.mapNotNull { tagString ->
                                    Tag.entries.find { it.name == tagString }
                                }

                                val book = Book(name, author, tagList)
                                //bookList[book.name] = book
                                bookList.add(book)
                                bookNamesList.add(book.name)
                                searchBookNameList.add(book.name)
                            }
                        }
                    }
                }

                val bookAdapter = ArrayAdapter<String>(
                    this@PreferredUserBooksActivity,
                    android.R.layout.simple_list_item_1,
                    userBookNamesList.toList()
                )
                binding.listview.adapter = bookAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })


        binding.listview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                startActivity(
                    Intent(
                        this@PreferredUserBooksActivity,
                        CardWatchActivity::class.java
                    ).apply {
                        putExtra("bookName", selectedItem)
                        putExtra("backActivityName", "Preferred list ")
                        putExtra("action", "removing")
                        putExtra("backActivity", PreferredUserBooksActivity::class.java)
                    })
            }

        binding.searchingStroke.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchString = s.toString()

                searchBookNameList.clear()

                for (item in userBookNamesList) {
                    for (bookName in bookNamesList) {
                        if (bookName.contains(searchString, ignoreCase = true) && bookName == item) {
                            searchBookNameList.add(bookName)
                        }
                    }
                }

                val bookAdapter = ArrayAdapter<String>(
                    this@PreferredUserBooksActivity,
                    android.R.layout.simple_list_item_1,
                    searchBookNameList.toList()
                )
                binding.listview.adapter = bookAdapter
            }

            override fun afterTextChanged(s: Editable?) {
                // Не используется
            }
        })

        setContentView(binding.root)
    }
}