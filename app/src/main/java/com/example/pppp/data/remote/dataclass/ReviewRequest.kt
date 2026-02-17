package com.example.pppp.data.remote.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class ReviewRequest(
    val userId: Long,
    val movieId: Long,
    val text: String,
    val stars: Int
)