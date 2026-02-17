package com.example.pppp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Retrofit {
    const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiAuth: AuthApi = retrofit.create(AuthApi::class.java)
    val Movies: MoviesApi = retrofit.create(MoviesApi::class.java)
    val Reviews: ReviewsApi = retrofit.create(ReviewsApi::class.java)
    val Users: UserApi = retrofit.create(UserApi::class.java)
}
