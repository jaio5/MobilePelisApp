package com.example.pppp.data.repository

import com.example.pppp.data.remote.AuthApi
import com.example.pppp.data.remote.dataclass.AuthResponse
import com.example.pppp.data.remote.dataclass.LoginRequest
import com.example.pppp.data.remote.dataclass.RegisterRequest
import com.example.pppp.data.remote.dataclass.RefreshRequest

class AuthRepository(private val api: AuthApi) {

    suspend fun login(request: LoginRequest): AuthResponse? {
        val response = api.login(request)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun register(request: RegisterRequest): AuthResponse? {
        val response = api.register(request)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun refresh(request: RefreshRequest): AuthResponse? {
        val response = api.refresh(request)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}