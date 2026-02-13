package com.example.pelisapp.data.remote.dataclass

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)
