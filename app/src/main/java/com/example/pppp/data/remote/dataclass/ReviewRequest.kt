package com.example.pppp.data.remote.dataclass

import com.google.gson.annotations.SerializedName

data class ReviewRequest(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("movieId")
    val movieId: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("stars")
    val stars: Int
)