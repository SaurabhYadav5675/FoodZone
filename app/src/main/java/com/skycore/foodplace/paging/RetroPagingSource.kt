package com.skycore.foodplace.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.skycore.foodplace.MainActivity
import com.skycore.foodplace.apihelper.ApiService
import com.skycore.foodplace.models.ApiRequest
import com.skycore.foodplace.models.Businesse
import retrofit2.HttpException
import java.io.IOException

class RetroPagingSource(
    private val apiService: ApiService,
    private val apiRequest: ApiRequest
) : PagingSource<Int, Businesse>() {

    /**
     *Provide a Key used for the initial load for the next PagingSource
     * Try to find the page key of the closest page to anchorPosition
     * prevKey == null -> anchorPage is the first page.
     * nextKey == null -> anchorPage is the last page.
     * both prevKey and nextKey null -> anchorPage is the initial page, so just return null.
     */
    override fun getRefreshKey(state: PagingState<Int, Businesse>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Businesse> {
        try {
            val position = params.key ?: 0

            apiRequest.offset = (15 * position).toString()
            val mapParams = apiRequest.toMap() as Map<String, String>

            val response = apiService.getRestaurants(mapParams)
            val isEmpty = response.businesses.isEmpty()

            return LoadResult.Page(
                data = response.businesses,
                prevKey = if (position == 1) null else position - 1,//use null for Only paging forward.
                nextKey = if (position == response.businesses.size) null else if (isEmpty) null else position + 1
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            return LoadResult.Error(e)
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}


