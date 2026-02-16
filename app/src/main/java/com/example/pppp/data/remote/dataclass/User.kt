package com.example.pppp.data.remote.dataclass

data class User(
    val id: Long,
    val username: String,
    val displayName: String?,
    val criticLevel: Int?,
    val email: String?,
    val roles: List<String>
)
