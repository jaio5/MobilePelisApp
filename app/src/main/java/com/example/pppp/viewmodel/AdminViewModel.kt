package com.example.pppp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.remote.dataclass.User
import com.example.pppp.data.repository.UserRepository
import com.example.pppp.uiState.AdminUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState

    fun loadUsers(token: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getAllUsers(token)
                if (response.isSuccessful) {
                    _uiState.value = AdminUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = AdminUiState.Error(response.message())
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateUser(token: String, user: User) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.updateUser(token, user.id, user)
                if (response.isSuccessful) {
                    _uiState.value = AdminUiState.UserUpdated(response.body()!!)
                } else {
                    _uiState.value = AdminUiState.Error(response.message())
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteUser(token: String, userId: Long, currentUserId: Long) {
        if (userId == currentUserId) {
            _uiState.value = AdminUiState.SelfDeleteError
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteUser(token, userId)
                if (response.isSuccessful) {
                    _uiState.value = AdminUiState.UserDeleted(userId)
                } else {
                    _uiState.value = AdminUiState.Error(response.message())
                }
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
