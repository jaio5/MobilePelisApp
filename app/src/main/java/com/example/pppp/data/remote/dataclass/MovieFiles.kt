package com.example.pppp.data.remote.dataclass

data class MovieFiles(
    val movieId: Long,
    val files: List<MovieFile>,
    val totalFiles: Int
)