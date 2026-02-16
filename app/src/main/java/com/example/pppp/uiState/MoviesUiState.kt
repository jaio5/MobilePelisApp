package com.example.pppp.uiState

import com.example.pppp.data.remote.dataclass.MoviePaginatedResponse

sealed class MoviesUiState {
    object Idle : MoviesUiState()
    object Loading : MoviesUiState()
    data class Success(val response: MoviePaginatedResponse) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
}
