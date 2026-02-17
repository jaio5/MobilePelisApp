package com.example.pppp.data.remote.dataclass

import com.google.gson.annotations.SerializedName

data class MovieFiles(
    @SerializedName("files")
    val files: List<MovieFile>
)