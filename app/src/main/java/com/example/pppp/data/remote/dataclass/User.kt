package com.example.pppp.data.remote.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val username: String,
    val displayName: String?,
    val criticLevel: Int?,
    val email: String?,
    val roles: List<String>
)
