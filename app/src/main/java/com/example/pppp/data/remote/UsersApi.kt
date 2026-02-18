package com.example.pppp.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UsersApi {
    @GET("/api/admin/users/search/username")
    suspend fun getUserByUsername(
        @Query("value") username: String,
        @Header("Authorization") token: String
    ): Response<UserSearchResponse>
}

data class UserSearchResponse(
    val users: List<UserShort>
)
data class UserShort(
    val id: Long,
    val username: String
)

