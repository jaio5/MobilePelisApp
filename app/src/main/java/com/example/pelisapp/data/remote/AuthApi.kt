package com.example.pelisapp.data.remote

import com.example.pelisapp.data.remote.dataclass.AuthResponse
import com.example.pelisapp.data.remote.dataclass.LoginRequest
import com.example.pelisapp.data.remote.dataclass.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthResponse>
}