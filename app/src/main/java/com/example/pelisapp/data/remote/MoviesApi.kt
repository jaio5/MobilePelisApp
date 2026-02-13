package com.example.pelisapp.data.remote

import com.example.pelisapp.data.remote.dataclass.Movie
import com.example.pelisapp.data.remote.dataclass.MovieFiles
import com.example.pelisapp.data.remote.dataclass.PaginatedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MoviesApi {
    @GET("/api/movies")
    suspend fun getMovies(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PaginatedResponse<Movie>>

    @GET("/api/movies/{id}/details")
    suspend fun getMovieDetails(@Path("id") id: Long): Response<Movie>

    @GET("/api/movies/{id}/files")
    suspend fun getMovieFiles(@Path("id") id: Long): Response<MovieFiles>

    @GET("/api/movies/by-category")
    suspend fun getMoviesByCategory(
        @Query("category") category: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PaginatedResponse<Movie>>
}