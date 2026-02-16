package com.example.pppp.data.remote.dataclass

data class Movie(
    val id: Long,
    val title: String,
    val overview: String? = null,
    val posterUrl: String? = null
)