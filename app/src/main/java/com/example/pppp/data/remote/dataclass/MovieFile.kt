package com.example.pppp.data.remote.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class MovieFile(
    val name: String,
    val size: Long,
    val downloadUrl: String,
    val streamUrl: String
)
