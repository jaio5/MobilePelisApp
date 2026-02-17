package com.example.pppp.data.remote.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
