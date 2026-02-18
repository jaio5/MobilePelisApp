package com.example.pppp.data.remote

import com.example.pppp.data.remote.dataclass.AdminUsersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApi {
    @POST("/api/admin/tmdb/load-movie/{tmdbId}")
    suspend fun loadMovieFromTmdb(
        @Path("tmdbId") tmdbId: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("/api/admin/tmdb/bulk-load")
    suspend fun bulkLoadMovies(
        @Query("page") page: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("/api/admin/images/reload")
    suspend fun reloadPosters(
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("/api/admin/users/{userId}/confirm-email")
    suspend fun confirmUserEmail(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("/api/admin/users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): Response<AdminUsersResponse>
}