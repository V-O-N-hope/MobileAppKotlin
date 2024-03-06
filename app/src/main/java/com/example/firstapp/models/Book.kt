package com.example.firstapp.models

data class Book(
    val name: String,
    val author: String,
    val tags: List<Tag>
)