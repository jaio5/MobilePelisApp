package com.example.pppp.data.remote.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class MovieFiles(
    val movieId: Long,
    val files: List<MovieFile>,
    val totalFiles: Int
)