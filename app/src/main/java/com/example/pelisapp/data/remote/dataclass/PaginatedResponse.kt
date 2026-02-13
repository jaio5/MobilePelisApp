package com.example.pelisapp.data.remote.dataclass

data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
