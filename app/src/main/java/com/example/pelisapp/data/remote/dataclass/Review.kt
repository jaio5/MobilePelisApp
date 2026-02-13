package com.example.pelisapp.data.remote.dataclass

data class Review(
    val id: Long,
    val userId: Long,
    val movieId: Long,
    val text: String,
    val stars: Int
)