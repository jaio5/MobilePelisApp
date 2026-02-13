package com.example.pelisapp.data.repository

import com.example.pelisapp.data.remote.MoviesApi
import com.example.pelisapp.data.remote.dataclass.Movie
import com.example.pelisapp.data.remote.dataclass.MovieFiles
import com.example.pelisapp.data.remote.dataclass.PaginatedResponse
import retrofit2.Response

class MoviesRepository(private val api: MoviesApi) {

    suspend fun getMovies(page: Int, size: Int): Response<PaginatedResponse<Movie>> {
        return api.getMovies(page, size)
    }

    suspend fun getMovieDetails(id: Long): Response<Movie>{
        return api.getMovieDetails(id)
    }

    suspend fun getMovieFiles(id: Long): Response<MovieFiles>{
        return api.getMovieFiles(id)
    }

    suspend fun getMoviesByCategory(category: String, page: Int, size: Int): Response<PaginatedResponse<Movie>>{
        return api.getMoviesByCategory(category, page, size)
    }
}