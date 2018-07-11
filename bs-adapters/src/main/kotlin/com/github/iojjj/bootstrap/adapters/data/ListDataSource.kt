@file:Suppress("NOTHING_TO_INLINE")

package com.github.iojjj.bootstrap.adapters.data

import android.arch.paging.DataSource
import android.arch.paging.PositionalDataSource

/**
 * Implementation of [PositionalDataSource] that displays a fixed list of items.
 */
class ListDataSource<T> private constructor(private val items: List<T>) : PositionalDataSource<T>() {

    companion object {

        /**
         * Create a new instance of `ListDataSource`.
         *
         * @param items items to wrap in `DataSource`
         *
         * @return a new instance of `ListDataSource`
         */
        @JvmStatic
        fun <T> of(items: Iterable<T>) = ListDataSource(items.toList())

        /**
         * Create a new instance of `ListDataSource`.
         *
         * @param items items to wrap in `DataSource`
         *
         * @return a new instance of `ListDataSource`
         */
        @JvmStatic
        fun <T> of(items: Array<T>) = of(items.asIterable())
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
        val totalSize = items.size
        val startPosition = computeInitialLoadPosition(params, totalSize)
        val count = computeInitialLoadSize(params, startPosition, totalSize)
        val result = loadRange(startPosition, count)
        callback.onResult(result, startPosition, totalSize)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
        callback.onResult(loadRange(params.startPosition, params.loadSize))
    }

    private fun loadRange(startPosition: Int, count: Int): List<T> {
        val endPosition = startPosition + count
        return items.subList(startPosition, endPosition)
    }
}

/**
 * Create a new instance of `ListDataSource`.
 *
 * @param items items to wrap in `DataSource`
 *
 * @return a new instance of `ListDataSource`
 */
inline fun <T> listDataSourceOf(items: Iterable<T>): DataSource<Int, T> = ListDataSource.of(items)