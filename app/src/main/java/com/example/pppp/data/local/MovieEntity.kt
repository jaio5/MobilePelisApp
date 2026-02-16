package com.example.pppp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val overview: String?,
    val posterUrl: String?,
    val createdAt: String?,
    val createdBy: String?
)
