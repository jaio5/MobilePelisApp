package com.example.pelisapp.data.repository

import com.example.pelisapp.data.remote.AuthApi
import com.example.pelisapp.data.remote.dataclass.AuthResponse
import com.example.pelisapp.data.remote.dataclass.LoginRequest
import com.example.pelisapp.data.remote.dataclass.RegisterRequest
import retrofit2.Response

class AuthRepository(private val api: AuthApi) {

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return api.login(request)
    }

    suspend fun register(request: RegisterRequest): Response<AuthResponse>{
        return api.register(request)
    }

    suspend fun refresh(request: RegisterRequest): Response<AuthResponse> {
        return api.refresh(request)
    }
}