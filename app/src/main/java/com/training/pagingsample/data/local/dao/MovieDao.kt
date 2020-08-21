package com.training.pagingsample.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.training.pagingsample.data.model.Movie

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovies(movies: List<Movie>)

    @Query("SELECT * FROM movies ORDER BY vote_count DESC")
    fun loadMovies() : PagingSource<Int, Movie>

    @Query("DELETE FROM movies")
    suspend fun clearMovies()
}