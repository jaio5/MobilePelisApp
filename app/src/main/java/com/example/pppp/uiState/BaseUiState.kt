package com.example.pppp.uiState

sealed class BaseUiState<out T> {
    object Idle : BaseUiState<Nothing>()
    object Loading : BaseUiState<Nothing>()
    data class Success<T>(val data: T) : BaseUiState<T>()
    data class Error(val message: String) : BaseUiState<Nothing>()
}
