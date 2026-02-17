package com.example.pppp.data.remote.dataclass

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("posterUrl")
    val posterUrl: String?
)