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

    // Buscar usuario por email (admin)
    @GET("/api/admin/users/search/email")
    suspend fun searchUserByEmail(
        @Header("Authorization") token: String,
        @Query("value") email: String
    ): Response<User>

    // Buscar usuario por username (admin)
    @GET("/api/admin/users/search/username")
    suspend fun searchUserByUsername(
        @Header("Authorization") token: String,
        @Query("value") username: String
    ): Response<User>

    @PUT("/api/users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body user: User
    ): Response<User>

    // Eliminar usuario (admin)
    @POST("/api/admin/users/{userId}/delete")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("userId") id: Long
    ): Response<Unit>

    // Banear usuario (admin)
    @POST("/api/admin/users/{userId}/ban")
    suspend fun banUser(
        @Header("Authorization") token: String,
        @Path("userId") id: Long
    ): Response<User>

    // Obtener todos los usuarios (admin)
    @GET("/api/admin/users")
    suspend fun getAllUsers(@Header("Authorization") token: String): Response<List<User>>
}