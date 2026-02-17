package com.example.pppp.data.remote.dataclass

data class Review(
    val id: Long,
    val userId: Long,
    val movieId: Long,
    val text: String,
    val stars: Int,
    val createdAt: String,
    val username: String? = null
)