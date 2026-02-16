package com.example.pppp.data.remote.dataclass

data class ReviewRequest(
    val userId: Long,
    val movieId: Long,
    val text: String,
    val stars: Int
)