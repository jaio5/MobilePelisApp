package com.example.pppp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val displayName: String?,
    val criticLevel: Int?,
    val email: String?,
    val roles: String
)
