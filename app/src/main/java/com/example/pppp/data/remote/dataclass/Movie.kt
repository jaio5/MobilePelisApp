package com.example.pppp.data.remote.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Long,
    val title: String,
    val overview: String? = null,
    val posterUrl: String? = null
)