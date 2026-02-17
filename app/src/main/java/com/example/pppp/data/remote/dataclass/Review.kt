package com.example.pppp.data.remote.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: Long,
    val userId: Long,
    val movieId: Long,
    val text: String,
    val stars: Int,
    val createdAt: String,
    val username: String? = null
)