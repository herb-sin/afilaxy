package com.afilaxy.presentation.helper

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.afilaxy.data.database.HelperDao
import com.afilaxy.data.database.HelperEntity
import com.afilaxy.domain.model.Helper

class HelpersPagingSource(
    private val helperDao: HelperDao
) : PagingSource<Int, Helper>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Helper> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            
            val helpers = helperDao.getAvailableHelpers()
                .drop(page * pageSize)
                .take(pageSize)
                .map { entity ->
                    Helper(
                        id = entity.id,
                        nome = entity.nome,
                        distanciaEstimada = "${entity.distanciaMetros.toInt()}m",
                        distanciaMetros = entity.distanciaMetros
                    )
                }
            
            LoadResult.Page(
                data = helpers,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (helpers.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, Helper>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}