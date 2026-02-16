package com.example.pppp.data.remote.dataclass

data class Review(
    val id: Long,
    val userId: Long,
    val movieId: Long,
    val text: String,
     val stars: Int,
    val username: String? = null
)