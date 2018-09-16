package com.github.iojjj.bootstrap.adapters.data

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.paging.PagedList
import com.github.iojjj.bootstrap.adapters.data.providers.LiveDataProvider
import com.github.iojjj.bootstrap.adapters.data.providers.PagedListLiveDataProvider
import com.github.iojjj.bootstrap.adapters.data.providers.map
import com.github.iojjj.bootstrap.adapters.data.providers.toLiveDataProvider
import java.util.concurrent.Executor

internal class PagedListLiveData<T>(private val delegate: ConfigLiveData<PagedList<T>?>) : ConfigLiveData<PagedList<T>?> by delegate {

    companion object {

        /**
         * Create a new instance of [SingleLiveData] builder intended to be used with [PagedList]s.
         *
         * @return a new instance of `SingleLiveData` builder
         */
        fun <K, V> newBuilder(): ConfigLiveData.StageDataSourcePagedList<K, V> = PagedListBuilder()
    }

    ///
    /// PagedList builder implementation
    ///

    private class PagedListBuilder<K, V>
        :
            ConfigLiveData.StageDataSourcePagedList<K, V>,
            ConfigLiveData.StageFetchExecutorPagedList<K, V>,
            ConfigLiveData.StageNotifyExecutorPagedList<K, V>,
            ConfigLiveData.StageConfigPagedList<K, V>,
            ConfigLiveData.StageFinalPagedList<K, V> {

        private lateinit var delegate: LiveDataProvider<DataSource<K, V>>
        private lateinit var config: PagedList.Config
        private lateinit var notifyExecutor: Executor
        private lateinit var fetchExecutor: Executor
        private var initialKey: K? = null
        private var initialConfig: Map<String, Any?>? = null
        private var boundaryCallback: PagedList.BoundaryCallback<V>? = null
        private var observeInstantly = false

        override fun withDataSourceFactory(dataFactory: ConfigLiveData.DataFactory<DataSource<K, V>>): ConfigLiveData.StageConfigPagedList<K, V> {
            this.delegate = dataFactory.toLiveDataProvider()
            return this
        }

        @Suppress("UNCHECKED_CAST")
        override fun withLiveData(liveData: LiveData<out Collection<V>>): ConfigLiveData.StageConfigPagedList<K, V> {
            this.delegate = liveData.toLiveDataProvider().map { ListDataSource.of(it) } as LiveDataProvider<DataSource<K, V>>
            return this
        }

        override fun withConfig(config: PagedList.Config): ConfigLiveData.StageFetchExecutorPagedList<K, V> {
            this.config = config
            return this
        }

        override fun withPageSize(pageSize: Int): ConfigLiveData.StageFetchExecutorPagedList<K, V> {
            this.config = PagedList.Config.Builder()
                    .setPageSize(pageSize)
                    .build()
            return this
        }

        override fun withFetchExecutor(executor: Executor): ConfigLiveData.StageNotifyExecutorPagedList<K, V> {
            this.fetchExecutor = executor
            return this
        }

        override fun withNotifyExecutor(executor: Executor): ConfigLiveData.StageFinalPagedList<K, V> {
            this.notifyExecutor = executor
            return this
        }

        override fun withInitialKey(initialKey: K): ConfigLiveData.StageFinalPagedList<K, V> {
            this.initialKey = initialKey
            return this
        }

        override fun withInitialConfig(initialConfig: Map<String, Any?>): ConfigLiveData.StageFinalPagedList<K, V> {
            this.initialConfig = initialConfig
            return this
        }

        override fun withBoundaryCallback(boundaryCallback: PagedList.BoundaryCallback<V>): ConfigLiveData.StageFinalPagedList<K, V> {
            this.boundaryCallback = boundaryCallback
            return this
        }

        override fun withObserveInstantly(observeInstantly: Boolean): PagedListBuilder<K, V> {
            this.observeInstantly = observeInstantly
            return this
        }

        override fun build(): ConfigLiveData<PagedList<V>?> {
            val provider = PagedListLiveDataProvider(delegate, config, fetchExecutor, notifyExecutor, initialKey, boundaryCallback)
            val delegate = SingleLiveData(provider, observeInstantly, fetchExecutor, notifyExecutor)
            return PagedListLiveData(delegate).apply {
                initialConfig?.let(configuration::load)
            }
        }
    }
}