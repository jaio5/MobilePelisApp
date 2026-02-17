package com.example.pppp.data.remote.dataclass

import com.google.gson.annotations.SerializedName

data class PagedResponse<T>(
    @SerializedName("content")
    val content: List<T>,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("totalElements")
    val totalElements: Int,
    @SerializedName("last")
    val last: Boolean,
    @SerializedName("first")
    val first: Boolean,
    @SerializedName("size")
    val size: Int,
    @SerializedName("number")
    val number: Int
)
