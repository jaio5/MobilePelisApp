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

/**
 * Sealed class to distinguish between "not yet submitted" and "error on submit"
 * for review posting, so the UI can react correctly to both states.
 */
sealed class ReviewPostState {
    object Idle : ReviewPostState()
    object Loading : ReviewPostState()
    data class Success(val review: Review) : ReviewPostState()
    data class Error(val message: String) : ReviewPostState()
}

class MoviesViewModel(private val repository: MoviesRepository) : ViewModel() {

    // ✅ currentPage is 1-based for display; we convert to 0-based when calling the API
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _movies = MutableStateFlow<Response<MoviePaginatedResponse>?>(null)
    val movies: StateFlow<Response<MoviePaginatedResponse>?> = _movies

    private val _movieDetails = MutableStateFlow<Response<Movie>?>(null)
    val movieDetails: StateFlow<Response<Movie>?> = _movieDetails

    private val _movieFiles = MutableStateFlow<Response<MovieFiles>?>(null)
    val movieFiles: StateFlow<Response<MovieFiles>?> = _movieFiles

    private val _movieReviews = MutableStateFlow<Response<List<Review>>?>(null)
    val movieReviews: StateFlow<Response<List<Review>>?> = _movieReviews

    // ✅ Using a proper sealed class instead of nullable Response to avoid ambiguity
    private val _reviewPostState = MutableStateFlow<ReviewPostState>(ReviewPostState.Idle)
    val reviewPostState: StateFlow<ReviewPostState> = _reviewPostState

    // Keep for backwards compatibility with MovieDetailScreen that observes reviewPostResult
    private val _reviewPostResult = MutableStateFlow<Response<Review>?>(null)
    val reviewPostResult: StateFlow<Response<Review>?> = _reviewPostResult

    private val _moviesByGenre = MutableStateFlow<Map<String, List<Movie>>>(emptyMap())
    val moviesByGenre: StateFlow<Map<String, List<Movie>>> = _moviesByGenre

    /**
     * Fetch movies. page is 1-based from UI; API expects 0-based.
     */
    fun getMovies(page: Int, size: Int = 12) {
        viewModelScope.launch {
            try {
                // ✅ FIX: page parameter here is already 0-based (caller converts)
                val response = repository.getMovies(page, size)
                Log.d("MOVIES", "Page $page -> ${response.body()?.content?.size} items, total pages: ${response.body()?.totalPages}")
                _movies.value = response
            } catch (e: Exception) {
                Log.e("MOVIES", "Error fetching movies", e)
            }
        }
    }

    fun getMovieDetails(id: Long) {
        viewModelScope.launch {
            try {
                _movieDetails.value = null // reset while loading
                _movieDetails.value = repository.getMovieDetails(id)
            } catch (e: Exception) {
                Log.e("MOVIES", "Error fetching movie details", e)
            }
        }
    }

    fun getMovieFiles(id: Long) {
        viewModelScope.launch {
            try {
                _movieFiles.value = repository.getMovieFiles(id)
            } catch (e: Exception) {
                Log.e("MOVIES", "Error fetching movie files", e)
            }
        }
    }

    fun getMoviesByCategory(category: String, page: Int, size: Int) {
        viewModelScope.launch {
            try {
                _movies.value = repository.getMoviesByCategory(category, page, size)
            } catch (e: Exception) {
                Log.e("MOVIES", "Error fetching movies by category", e)
            }
        }
    }

    fun getMovieReviews(movieId: Long) {
        viewModelScope.launch {
            try {
                _movieReviews.value = null // reset while loading
                _movieReviews.value = repository.getMovieReviews(movieId)
            } catch (e: Exception) {
                Log.e("MOVIES", "Error fetching reviews", e)
            }
        }
    }

    fun postReview(review: ReviewRequest, token: String) {
        viewModelScope.launch {
            _reviewPostState.value = ReviewPostState.Loading
            try {
                val response = repository.postReview(review, token)
                if (response.isSuccessful && response.body() != null) {
                    _reviewPostResult.value = response
                    _reviewPostState.value = ReviewPostState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("MOVIES", "Post review error: $errorBody")
                    _reviewPostResult.value = response // keep for backward compat
                    _reviewPostState.value = ReviewPostState.Error(errorBody)
                }
            } catch (e: Exception) {
                Log.e("MOVIES", "Exception posting review", e)
                _reviewPostResult.value = null
                _reviewPostState.value = ReviewPostState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    fun nextPage() {
        _currentPage.value = _currentPage.value + 1
    }

    fun prevPage() {
        _currentPage.value = (_currentPage.value - 1).coerceAtLeast(1)
    }

    fun resetReviewState() {
        _reviewPostState.value = ReviewPostState.Idle
        _reviewPostResult.value = null
    }
}