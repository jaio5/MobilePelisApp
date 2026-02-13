package com.example.pelisapp.data.remote

import com.example.pelisapp.data.remote.dataclass.PaginatedResponse
import com.example.pelisapp.data.remote.dataclass.Review
import com.example.pelisapp.data.remote.dataclass.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UserApi {
    @GET("/api/users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<User>

    @GET("/api/users/me/reviews")
    suspend fun getMyReviews(
        @Header("Authorization") token: String,
        @retrofit2.http.Query("page") page: Int,
        @retrofit2.http.Query("size") size: Int
    ): Response<PaginatedResponse<Review>>
}