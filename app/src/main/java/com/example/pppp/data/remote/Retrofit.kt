package com.example.pppp.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import kotlin.jvm.java

object Retrofit {
    const val BASE_URL = "http://10.0.2.2:8080/"
    const val CONTENT_TYPE = "application/json"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory(CONTENT_TYPE.toMediaType()))
        .build()

    val apiAuth: AuthApi = retrofit.create(AuthApi::class.java)
    val Movies: MoviesApi = retrofit.create(MoviesApi::class.java)
    val Reviews: ReviewsApi = retrofit.create(ReviewsApi::class.java)
    val Users: UserApi = retrofit.create(UserApi::class.java)
}
