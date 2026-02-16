package com.example.pppp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY createdAt DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Long): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' OR overview LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun filterMovies(query: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE createdBy = :username ORDER BY createdAt DESC")
    fun getMoviesByUser(username: String): Flow<List<MovieEntity>>
}
