package com.training.pagingsample.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.training.pagingsample.data.local.dao.MovieDao
import com.training.pagingsample.data.local.dao.RemoteKeysDao
import com.training.pagingsample.data.model.Movie
import com.training.pagingsample.data.model.RemoteKeys

@Database(entities = [Movie::class, RemoteKeys::class], version = 1)
abstract class MovieAppDB : RoomDatabase() {

    abstract fun getMovieDao() : MovieDao
    abstract fun getRemoteKeysDao() : RemoteKeysDao
}