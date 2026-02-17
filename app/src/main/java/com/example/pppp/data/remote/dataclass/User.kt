package com.example.pppp.data.remote.dataclass

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("displayName")
    val displayName: String?,
    @SerializedName("criticLevel")
    val criticLevel: Int?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("roles")
    val roles: List<String>
)
