package com.example.pppp.data.repository

import com.example.pppp.data.local.UserDao
import com.example.pppp.data.local.UserEntity

class UserLocalRepository(private val userDao: UserDao) {
    suspend fun saveUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun getUser(): UserEntity? = userDao.getUser()
    suspend fun clearUser() = userDao.clearUser()
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
}
