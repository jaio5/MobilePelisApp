package com.example.pppp.data.repository

import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.UserApi
import com.example.pppp.data.remote.dataclass.PaginatedResponse
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.User
import retrofit2.Response

class UserRepository(private val api: UserApi = Retrofit.Users) {

    suspend fun getMe(token: String): Response<User> {
        return api.getMe("Bearer $token")
    }

    suspend fun getMyReviews(token: String, page: Int, size: Int): Response<PaginatedResponse<Review>>{
        return api.getMyReviews("Bearer $token", page, size)
    }

    suspend fun getAllUsers(token: String): Response<List<User>> {
        return api.getAllUsers("Bearer $token")
    }

    suspend fun updateUser(token: String, id: Long, user: User): Response<User> {
        return api.updateUser("Bearer $token", id, user)
    }

    suspend fun deleteUser(token: String, id: Long): Response<Unit> {
        return api.deleteUser("Bearer $token", id)
    }
}