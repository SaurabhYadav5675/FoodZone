package com.skycore.foodplace.viewmodel

import androidx.lifecycle.*
import androidx.paging.*
import com.skycore.foodplace.apihelper.ApiService
import com.skycore.foodplace.models.ApiRequest
import com.skycore.foodplace.paging.RetroPagingSource

class MainViewModel(private val apiService: ApiService) : ViewModel() {
    //use to update query
    private var postListUrl = MutableLiveData<ApiRequest>()

    private val postUrl: LiveData<ApiRequest>
        get() = postListUrl


    val postList = postUrl.switchMap { query ->
        Pager(
            PagingConfig(
                pageSize = 15,
                maxSize = 45,
                enablePlaceholders = false,
                prefetchDistance = 5,
                initialLoadSize = 15
            )
        ) {
            RetroPagingSource(apiService, query)
        }.liveData.cachedIn(viewModelScope)
    }


    fun setCurrentQuery(query: ApiRequest) {
        postListUrl.postValue(query)
    }

}