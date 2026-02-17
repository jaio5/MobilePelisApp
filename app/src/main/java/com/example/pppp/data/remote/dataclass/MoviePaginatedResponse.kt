package com.example.pppp.data.remote.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class MoviePaginatedResponse(
    val content: List<Movie>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
