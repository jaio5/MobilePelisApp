package com.example.pppp.data.remote.dataclass

data class AdminUsersResponse(
    val users: List<User>,
    val totalElements: Int,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

