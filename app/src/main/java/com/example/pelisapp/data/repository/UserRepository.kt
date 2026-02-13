package com.example.pelisapp.data.repository

import com.example.pelisapp.data.remote.UserApi
import com.example.pelisapp.data.remote.dataclass.PaginatedResponse
import com.example.pelisapp.data.remote.dataclass.Review
import com.example.pelisapp.data.remote.dataclass.User
import retrofit2.Response

class UserRepository(private val api: UserApi) {

    suspend fun getMe(token: String): Response<User> {
        return api.getMe(token)
    }

    suspend fun getMyReviews(token: String, page: Int, size: Int): Response<PaginatedResponse<Review>>{
        return api.getMyReviews(token, page, size)
    }
}