package com.example.pppp.data.remote


import com.example.pppp.data.remote.dataclass.Movie
import com.example.pppp.data.remote.dataclass.MovieFiles
import com.example.pppp.data.remote.dataclass.MoviePaginatedResponse
import com.example.pppp.data.remote.dataclass.Review
import com.example.pppp.data.remote.dataclass.ReviewRequest
import com.example.pppp.data.remote.dataclass.PagedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MoviesApi {
    @GET("/api/movies")
    suspend fun getMovies(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<MoviePaginatedResponse>

    @GET("/api/movies/{id}/details")
    suspend fun getMovieDetails(@Path("id") id: Long): Response<Movie>

    @GET("/api/movies/{id}/files")
    suspend fun getMovieFiles(@Path("id") id: Long): Response<MovieFiles>

    @GET("/api/movies/by-category")
    suspend fun getMoviesByCategory(
        @Query("category") category: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<MoviePaginatedResponse>

    @GET("/api/movies/{id}/reviews")
    suspend fun getMovieReviews(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<PagedResponse<Review>>

    @POST("/api/reviews")
    suspend fun postReview(
        @Body review: ReviewRequest,
        @Header("Authorization") token: String
    ): Response<Review>
}