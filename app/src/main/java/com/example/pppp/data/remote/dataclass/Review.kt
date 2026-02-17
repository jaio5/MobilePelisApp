package com.example.pppp.data.remote.dataclass

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("id")
    val id: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("movieId")
    val movieId: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("stars")
    val stars: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("username")
    val username: String? = null
)