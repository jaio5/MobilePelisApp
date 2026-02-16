package com.example.pppp.data.remote

import com.example.pppp.data.remote.dataclass.PaginatedResponse
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("/api/users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<User>

    @GET("/api/users/me/reviews")
    suspend fun getMyReviews(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PaginatedResponse<Review>>

    // --- Administración ---
    @GET("/api/users")
    suspend fun getAllUsers(@Header("Authorization") token: String): Response<List<User>>

    @PUT("/api/users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body user: User
    ): Response<User>

    @DELETE("/api/users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>
}