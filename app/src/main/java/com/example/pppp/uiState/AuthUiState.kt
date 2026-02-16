package com.example.pppp.uiState

import com.example.pppp.data.remote.dataclass.AuthResponse

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val response: AuthResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
