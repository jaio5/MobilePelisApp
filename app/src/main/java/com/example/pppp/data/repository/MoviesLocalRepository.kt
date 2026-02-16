package com.example.pppp.data.repository

import com.example.pppp.data.local.MovieDao
import com.example.pppp.data.local.MovieEntity
import kotlinx.coroutines.flow.Flow

class MoviesLocalRepository(private val movieDao: MovieDao) {
    fun getAllMovies(): Flow<List<MovieEntity>> = movieDao.getAllMovies()
    suspend fun getMovieById(id: Long): MovieEntity? = movieDao.getMovieById(id)
    suspend fun insertMovie(movie: MovieEntity) = movieDao.insertMovie(movie)
    suspend fun updateMovie(movie: MovieEntity) = movieDao.updateMovie(movie)
    suspend fun deleteMovie(movie: MovieEntity) = movieDao.deleteMovie(movie)
    fun filterMovies(query: String): Flow<List<MovieEntity>> = movieDao.filterMovies(query)
    fun getMoviesByUser(username: String): Flow<List<MovieEntity>> = movieDao.getMoviesByUser(username)
}
