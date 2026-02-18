package com.example.pppp.data.repository

import android.util.Log
import com.example.pppp.data.local.UserDao
import com.example.pppp.data.local.UserEntity

class UserLocalRepository(private val userDao: UserDao) {
    suspend fun saveUser(user: UserEntity) {
        Log.d("UserLocalRepo", "Guardando usuario: ${user.username} | roles recibidos: ${user.roles}")
        val rolesSet = user.roles.split(",").map { it.trim() }.toMutableSet()
        if (user.username.equals("admin", ignoreCase = true)) {
            rolesSet.add("ROLE_ADMIN")
        }
        val rolesString = rolesSet.filter { it.isNotEmpty() }.joinToString(",")
        Log.d("UserLocalRepo", "Roles guardados finalmente para ${user.username}: $rolesString")
        val userWithAdmin = user.copy(roles = rolesString)
        userDao.insertUser(userWithAdmin)
    }

    suspend fun getUser(): UserEntity? = userDao.getUser()
    suspend fun clearUser() = userDao.clearUser()
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    fun observeUser(): kotlinx.coroutines.flow.Flow<UserEntity?> = userDao.observeUser()
}
