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

    suspend fun updateUser(token: String, id: Long, user: User): Response<User> {
        return api.updateUser("Bearer $token", id, user)
    }

    suspend fun deleteUser(token: String, id: Long): Response<Unit> {
        return api.deleteUser("Bearer $token", id)
    }

    suspend fun banUser(token: String, id: Long): Response<User> {
        return api.banUser("Bearer $token", id)
    }

    suspend fun searchUserByEmail(token: String, email: String): Response<User> {
        return api.searchUserByEmail("Bearer $token", email)
    }

    suspend fun searchUserByUsername(token: String, username: String): Response<User> {
        return api.searchUserByUsername("Bearer $token", username)
    }

    suspend fun getAllUsers(token: String): Response<List<User>> {
        return api.getAllUsers("Bearer $token")
    }
}