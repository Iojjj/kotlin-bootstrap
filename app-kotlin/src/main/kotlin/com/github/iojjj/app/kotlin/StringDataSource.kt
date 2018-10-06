package com.github.iojjj.app.kotlin

import android.arch.paging.DataSource
import android.arch.paging.PositionalDataSource
import com.github.iojjj.app.core.BaseRecyclerViewActivity.DATA_SET_SIZE
import com.github.iojjj.app.core.BaseRecyclerViewActivity.KEY_REMOVED
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter
import com.github.iojjj.bootstrap.adapters.data.ConfigLiveData
import com.github.iojjj.bootstrap.adapters.data.Configuration

class StringDataSource(private val data: List<String>) : PositionalDataSource<String>() {

    companion object DataFactory : ConfigLiveData.DataFactory<DataSource<Int, String>> {

        private val data: List<String> by lazy { (0..DATA_SET_SIZE).map { "Item ${(it + 1)}" } }

        override fun create(configuration: Configuration): StringDataSource {
            val removed: List<String>? = configuration[KEY_REMOVED]
            val filter: String? = configuration[PagedAdapter.CONFIG_KEY_FILTER]
            return if (filter == null && (removed == null || removed.isEmpty())) {
                StringDataSource(data)
            } else {
                val stringFilter: (String) -> Boolean =
                        { (filter == null || it.contains(filter, true)) && (removed == null || !removed.contains(it)) }
                val filteredData = data.filter(stringFilter)
                StringDataSource(filteredData)
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<String>) {
        val size = data.size
        val startPosition = computeInitialLoadPosition(params, size)
        val loadSize = computeInitialLoadSize(params, startPosition, size)
        callback.onResult(data.subList(startPosition, loadSize), startPosition, size)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<String>) {
        callback.onResult(data.subList(params.startPosition, params.startPosition + params.loadSize))
    }
}