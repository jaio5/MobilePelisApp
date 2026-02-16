package com.example.pppp.data.remote.dataclass

data class MoviePaginatedResponse(
    val content: List<Movie>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
