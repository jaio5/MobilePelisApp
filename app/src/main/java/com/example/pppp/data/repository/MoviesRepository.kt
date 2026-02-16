package com.example.pppp.data.repository

import com.example.pppp.data.remote.MoviesApi
import com.example.pppp.data.remote.dataclass.Movie
import com.example.pppp.data.remote.dataclass.MovieFiles
import com.example.pppp.data.remote.dataclass.MoviePaginatedResponse
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
}