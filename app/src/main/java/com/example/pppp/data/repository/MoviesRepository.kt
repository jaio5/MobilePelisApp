package com.example.pppp.data.repository

import com.example.pppp.data.remote.MoviesApi
import com.example.pppp.data.remote.Retrofit
import com.example.pppp.data.remote.dataclass.Movie
import com.example.pppp.data.remote.dataclass.MovieFiles
import com.example.pppp.data.remote.dataclass.MoviePaginatedResponse
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.ReviewRequest
import com.example.pppp.data.remote.dataclass.PagedResponse
import retrofit2.Response

class MoviesRepository(private val api: MoviesApi = Retrofit.Movies) {

    private val reviewsRepository = ReviewsRepository()

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

    suspend fun getMovieReviews(movieId: Long): Response<List<Review>> {
        return reviewsRepository.getReviewsByMovie(movieId)
    }

    suspend fun postReview(review: ReviewRequest, token: String): Response<Review> {
        return reviewsRepository.createReview(review, token)
    }
}