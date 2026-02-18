package com.example.pppp.uiState

import com.example.pppp.data.remote.dataclass.User

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val users: List<User>) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
    data class UserUpdated(val user: User) : AdminUiState()
    data class UserDeleted(val userId: Long) : AdminUiState()
    object SelfDeleteError : AdminUiState()
    data class UserFound(val user: User) : AdminUiState()
}
