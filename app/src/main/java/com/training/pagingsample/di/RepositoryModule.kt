package com.training.pagingsample.di

import android.content.Context
import androidx.room.Room
import com.training.pagingsample.data.local.MovieAppDB
import com.training.pagingsample.data.local.dao.MovieDao
import com.training.pagingsample.data.local.dao.RemoteKeysDao
import com.training.pagingsample.data.network.MovieAppService
import com.training.pagingsample.data.repository.Repository
import org.koin.dsl.module

val repositoryModule = module {
    single { createRepository(get()) }
    single { createMovieDB(get()) }
    single { createMovieDao(get()) }
    single { createRemoteKeysDao(get()) }
}

fun createRepository(
    movieAppService: MovieAppService
) : Repository = Repository(movieAppService)

fun createMovieDB(
    context: Context
) : MovieAppDB = Room.databaseBuilder(
        context,
        MovieAppDB::class.java,
        "MovieAppDB"
    ).build()

fun createMovieDao(
    movieAppDB: MovieAppDB
) : MovieDao = movieAppDB.getMovieDao()

fun createRemoteKeysDao(
    movieAppDB: MovieAppDB
) : RemoteKeysDao = movieAppDB.getRemoteKeysDao()