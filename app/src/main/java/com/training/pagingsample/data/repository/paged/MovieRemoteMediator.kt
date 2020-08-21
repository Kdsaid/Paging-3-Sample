package com.training.pagingsample.data.repository.paged

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.training.pagingsample.data.local.MovieAppDB
import com.training.pagingsample.data.model.Movie
import com.training.pagingsample.data.model.RemoteKeys
import com.training.pagingsample.data.network.MovieAppService
import com.training.pagingsample.data.repository.Repository
import java.io.InvalidObjectException

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator(
    private val repository: Repository,
    private val movieAppDB: MovieAppDB
) : RemoteMediator<Int, Movie>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Movie>): MediatorResult {

        val page = when(loadType){
            LoadType.REFRESH -> {
                Log.d("debuggg", "refresh")
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }

            LoadType.APPEND -> {
                Log.d("debuggg", "append")
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys?.nextKey == null) {
                    throw InvalidObjectException("Remote key should not be null for $loadType")
                }
                remoteKeys.nextKey
            }

            LoadType.PREPEND -> {
                Log.d("debuggg", "prepend")
                val remoteKeys = getRemoteKeyForFirstItem(state)
                if (remoteKeys == null) {
                    throw InvalidObjectException("Remote key and the prevKey should not be null")
                }
                val prevKey = remoteKeys.prevKey
                if (prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
                remoteKeys.prevKey
            }
        }
        try{
            val movieResponse = repository.getPopularMovies(page)
            val movies = movieResponse.results
            val endOfPaginationReached = movies!!.isEmpty()

            Log.d("debuggg", "page: $page and loadType: ${loadType.name}")

            movieAppDB.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    movieAppDB.getRemoteKeysDao().clearRemoteKeys()
                    movieAppDB.getMovieDao().clearMovies()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = movies.map {
                    RemoteKeys(movieId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                movieAppDB.getRemoteKeysDao().saveKeys(keys)
                movieAppDB.getMovieDao().saveMovies(movies)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        }catch (e: Exception){
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Movie>) : RemoteKeys?{
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let {
                movieAppDB.getRemoteKeysDao().remoteKeysMovieId(it.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Movie>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { repo ->
                movieAppDB.getRemoteKeysDao().remoteKeysMovieId(repo.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Movie>
    ): RemoteKeys? {

        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                movieAppDB.getRemoteKeysDao().remoteKeysMovieId(repoId)
            }
        }
    }
}