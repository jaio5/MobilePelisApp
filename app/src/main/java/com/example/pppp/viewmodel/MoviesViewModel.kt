package com.example.pppp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.remote.dataclass.Movie
import com.example.pppp.data.remote.dataclass.MovieFiles
import com.example.pppp.data.remote.dataclass.MoviePaginatedResponse
import com.example.pppp.data.repository.MoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class MoviesViewModel (private val repository: MoviesRepository): ViewModel() {
    private var _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _moviesByGenre = MutableStateFlow<Map<String, List<Movie>>>(emptyMap())
    val moviesByGenre: StateFlow<Map<String, List<Movie>>> = _moviesByGenre

    private val _movies = MutableStateFlow<Response<MoviePaginatedResponse>?>(null)
    val movies: StateFlow<Response<MoviePaginatedResponse>?> = _movies

    private val _movieDetails = MutableStateFlow<Response<Movie>?>(null)
    val movieDetails: StateFlow<Response<Movie>?> = _movieDetails

    private val _movieFiles = MutableStateFlow<Response<MovieFiles>?>(null)
    val movieFiles: StateFlow<Response<MovieFiles>?> = _movieFiles

    fun getMovies(page: Int, size: Int) {
        viewModelScope.launch {
            val response = repository.getMovies(page, size)
            Log.d("MOVIES_DEBUG", "Respuesta de la API: " + response.body()?.content.toString())
            _movies.value = response
        }
    }

    fun getMovieDetails(id: Long) {
        viewModelScope.launch {
            _movieDetails.value = repository.getMovieDetails(id)
        }
    }

    fun getMovieFiles(id: Long) {
        viewModelScope.launch {
            _movieFiles.value = repository.getMovieFiles(id)
        }
    }

    fun getMoviesByCategory(category: String, page: Int, size: Int) {
        viewModelScope.launch {
            _movies.value = repository.getMoviesByCategory(category, page, size)
        }
    }

    fun nextPage() {
        val next = (_currentPage.value + 1)
        _currentPage.value = next
    }

    fun prevPage() {
        val prev = (_currentPage.value - 1).coerceAtLeast(1)
        _currentPage.value = prev
    }

}