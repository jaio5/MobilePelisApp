package com.example.pppp.data.remote.dataclass

import com.google.gson.annotations.SerializedName

data class UserSummary(
    @SerializedName("id") val id: Long?,
    @SerializedName("username") val username: String?
)

data class MovieSummary(
    @SerializedName("id") val id: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("posterUrl") val posterUrl: String?
)

data class Review(
    @SerializedName("id") val id: Long?,
    @SerializedName("user") val user: UserSummary?,
    @SerializedName("movie") val movie: MovieSummary?,
    @SerializedName("text") val text: String?,
    @SerializedName("stars") val stars: Int?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("likesCount") val likesCount: Long? = null,
    @SerializedName("likes") val likes: List<Any>? = null,
    @SerializedName("moderation") val moderation: Any? = null
)