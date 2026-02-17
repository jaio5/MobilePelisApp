package com.example.pppp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pppp.data.remote.dataclass.Movie
import com.example.pppp.data.remote.dataclass.MovieFiles
import com.example.pppp.data.remote.dataclass.MoviePaginatedResponse
import com.example.pppp.data.remote.dataclass.PagedResponse
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.ReviewRequest
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

    private val _movieReviews = MutableStateFlow<Response<PagedResponse<Review>>?>(null)
    val movieReviews: StateFlow<Response<PagedResponse<Review>>?> = _movieReviews

    private val _reviewPostResult = MutableStateFlow<Response<Review>?>(null)
    val reviewPostResult: StateFlow<Response<Review>?> = _reviewPostResult

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

    fun getMovieReviews(movieId: Long, token: String) {
        viewModelScope.launch {
            _movieReviews.value = repository.getMovieReviews(movieId, token)
        }
    }

    fun postReview(review: ReviewRequest, token: String) {
        viewModelScope.launch {
            try {
                val response = repository.postReview(review, token)
                if (response.isSuccessful) {
                    _reviewPostResult.value = response
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MOVIE_REVIEWS", "Error body: $errorBody")
                    _reviewPostResult.value = null
                }
            } catch (e: Exception) {
                android.util.Log.e("MOVIE_REVIEWS", "Excepción al enviar review", e)
                _reviewPostResult.value = null
            }
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