package com.example.pppp.data.remote.dataclass

import com.google.gson.annotations.SerializedName

data class MovieFile(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("size")
    val size: Long? = null // Añadido para reflejar el tamaño del archivo si lo provee la API
)
