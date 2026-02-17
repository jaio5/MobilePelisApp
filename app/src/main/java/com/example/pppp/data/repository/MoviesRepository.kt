package com.example.pppp.data.repository

import com.example.pppp.data.remote.MoviesApi
import com.example.pppp.data.remote.dataclass.Movie
import com.example.pppp.data.remote.dataclass.MovieFiles
import com.example.pppp.data.remote.dataclass.MoviePaginatedResponse
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.ReviewRequest
import com.example.pppp.data.remote.dataclass.PagedResponse
import retrofit2.Response

class MoviesRepository(private val api: MoviesApi) {

    suspend fun getMovies(page: Int, size: Int): Response<MoviePaginatedResponse> {
        return api.getMovies(page, size)
    }

    suspend fun getMovieDetails(id: Long): Response<Movie>{
        return api.getMovieDetails(id)
    }

    suspend fun getMovieFiles(id: Long): Response<MovieFiles>{
        return api.getMovieFiles(id)
    }

    suspend fun getMoviesByCategory(category: String, page: Int, size: Int): Response<MoviePaginatedResponse>{
        return api.getMoviesByCategory(category, page, size)
    }

    suspend fun getMovieReviews(movieId: Long, token: String): Response<PagedResponse<Review>> {
        return api.getMovieReviews(movieId, token)
    }

    suspend fun postReview(review: ReviewRequest, token: String): Response<Review> {
        try {
            val response = api.postReview(review, token)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("MOVIE_REVIEWS", "Error body: $errorBody")
            } else {
                android.util.Log.d("MOVIE_REVIEWS", "Review enviada correctamente: ${response.body()}")
            }
            return response
        } catch (e: Exception) {
            android.util.Log.e("MOVIE_REVIEWS", "Excepción al enviar review", e)
            throw e
        }
    }
}