package com.example.pelisapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pelisapp.data.remote.dataclass.Movie
import com.example.pelisapp.data.remote.dataclass.MovieFiles
import com.example.pelisapp.data.remote.dataclass.PaginatedResponse
import com.example.pelisapp.data.repository.MoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class MoviesViewModel (private val repository: MoviesRepository): ViewModel() {

    private val _moviesByGenre = MutableStateFlow<Map<String, List<Movie>>>(emptyMap())
    val moviesByGenre: StateFlow<Map<String, List<Movie>>> = _moviesByGenre

    private val _movies = MutableStateFlow<Response<PaginatedResponse<Movie>>?>(null)
    val movies: StateFlow<Response<PaginatedResponse<Movie>>?> = _movies

    private val _movieDetails = MutableStateFlow<Response<Movie>?>(null)
    val movieDetails: StateFlow<Response<Movie>?> = _movieDetails

    private val _movieFiles = MutableStateFlow<Response<MovieFiles>?>(null)
    val movieFiles: StateFlow<Response<MovieFiles>?> = _movieFiles

    fun getMovies(page: Int, size: Int) {
        viewModelScope.launch {
            _movies.value = repository.getMovies(page, size)
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

}