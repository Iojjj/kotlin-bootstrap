@file:Suppress("FunctionName", "unused", "UNCHECKED_CAST")

package com.github.iojjj.bootstrap.adapters.data

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.paging.DataSource
import android.arch.paging.PagedList
import android.support.annotation.MainThread
import com.github.iojjj.bootstrap.utils.Observable
import java.util.concurrent.Executor

/**
 * Implementation of [LiveData] that has its own [Configuration] object and invalidates every time configuration changes.
 *
 * @param T type of data
 */
interface ConfigLiveData<T> {

    companion object Factory {
        /**
         * Create a new instance of [ConfigLiveData] builder intended to be used with single values.
         *
         * @return a new instance of `ConfigLiveData` builder
         */
        fun <T> ofSingle(): StageDataSourceSingle<T> = SingleLiveData.newBuilder()

        /**
         * Create a new instance of [ConfigLiveData] builder intended to be used with [PagedList]s.
         *
         * @return a new instance of `ConfigLiveData` builder
         */
        fun <K, V> ofPagedList(): StageDataSourcePagedList<K, V> = PagedListLiveData.newBuilder()
    }

    /**
     * Configuration of `this` live data. Any changes to configuration will lead to [invalidate] method be invoked.
     */
    val configuration: Configuration
    /**
     * Observable responsible for notifying about initial load events.
     */
    val onInitialLoadObservable: Observable<OnInitialLoadObserver<T>>
    /**
     * Observable responsible for notifying about live data invalidation.
     */
    val onInvalidatedObservable: Observable<OnInvalidatedObserver>

    /**
     * Invalidates [ConfigLiveData].
     */
    fun invalidate()

    /**
     * Start observing `this` live data using provided [owner]. Observer will be automatically removed with [Lifecycle.Event.ON_DESTROY] event.
     *
     * @param owner instance of [LifecycleOwner]
     * @param observer instance of [Observer] that will receive callbacks from `LiveData`
     */
    fun observe(owner: LifecycleOwner, observer: Observer<T?>)

    /**
     * Remove previously registered observer.
     *
     * @param observer instance of [Observer]
     */
    fun removeObserver(observer: Observer<T?>)

    /**
     * Maps this [ConfigLiveData] to a new one.
     *
     * @param mapper mapper that converts produced values
     *
     * @return a new instance of [ConfigLiveData]
     */
    fun <R> map(mapper: (T) -> R): ConfigLiveData<R> = map(Mapper { mapper(it) })

    /**
     * Maps this [ConfigLiveData] to a new one.
     *
     * @param mapper mapper that converts produced values
     *
     * @return a new instance of [ConfigLiveData]
     */
    fun <R> map(mapper: DataMapper<T, R>): ConfigLiveData<R>

    /**
     * Observer that called when [ConfigLiveData] loads data for the first time.
     *
     * Callback methods will be invoked on main thread.
     */
    @MainThread
    interface OnInitialLoadObserver<T> {

        /**
         * Called when [ConfigLiveData] starts loading of the data.
         */
        fun onStartLoading()

        /**
         * Called when [ConfigLiveData] stops loading of the data.
         *
         * @param data loaded data or `null`
         */
        fun onStopLoading(data: T?)
    }

    /**
     * Empty implementation of [OnInitialLoadObserver].
     */
    abstract class OnInitialLoadAdapter<T> : OnInitialLoadObserver<T> {

        override fun onStartLoading() {
            /* no-op */
        }

        override fun onStopLoading(data: T?) {
            /* no-op */
        }

    }

    /**
     * Observer that called when [ConfigLiveData] is invalidated.
     */
    @FunctionalInterface
    interface OnInvalidatedObserver {

        /**
         * Called when [ConfigLiveData] is invalidated.
         */
        fun onLiveDataInvalidated()
    }

    /**
     * Factory used to produce values based on configuration.
     */
    @FunctionalInterface
    interface DataFactory<T> {

        /**
         * Create a new value using provided [configuration].
         *
         * @param configuration configuration object
         *
         * @return a new value
         */
        fun create(configuration: Configuration): T
    }

    /**
     * Mapper used to map values to a new type.
     *
     * @param T initial value type
     * @param R converted value type
     */
    @FunctionalInterface
    interface DataMapper<T, R> {

        /**
         * Map a value to a new type.
         *
         * @param value initial value
         *
         * @return converted value
         */
        fun map(value: T): R
    }

    ///
    /// Single builder interface
    ///

    /**
     * Builder stage that allows to set data provider.
     *
     * @param T type of data
     */
    interface StageDataSourceSingle<T> {

        /**
         * Set a single value that will be returned by [ConfigLiveData].
         *
         * @param data any data
         *
         * @return same builder for chaining calls
         */
        fun withJust(data: T?): StageFetchExecutorSingle<T>

        /**
         * Set a live data object that will be used as data provider for [ConfigLiveData].
         *
         * @param liveData live data that produces values
         *
         * @return same builder for chaining calls
         */
        fun withLiveData(liveData: LiveData<T?>): StageFetchExecutorSingle<T>

        /**
         * Set a factory that will be used as data provider for [ConfigLiveData].
         *
         * @param dataFactory factory that produces values
         *
         * @return same builder for chaining calls
         */
        fun withDataFactory(dataFactory: DataFactory<T?>): StageFetchExecutorSingle<T>

        /**
         * Set a factory that will be used as data provider for [ConfigLiveData].
         *
         * @param factory factory that produces values
         *
         * @return same builder for chaining calls
         */
        fun withDataFactory(factory: (Configuration) -> T?): StageFetchExecutorSingle<T> = withDataFactory(Factory { factory(it) })

    }

    /**
     * Builder stage that allows to set fetch executor.
     *
     * @param T type of data
     */
    interface StageFetchExecutorSingle<T> {

        /**
         * Set executor that will be used to fetch data from data provider.
         *
         * @param executor instance of [Executor]
         *
         * @return same builder for chaining calls
         */
        fun withFetchExecutor(executor: Executor): StageNotifyExecutorSingle<T>
    }

    /**
     * Builder stage that allows to set notify executor.
     *
     * @param T type of data
     */
    interface StageNotifyExecutorSingle<T> {

        /**
         * Set executor that will be used to notify when data loaded.
         *
         * @param executor instance of [Executor]
         *
         * @return same builder for chaining calls
         */
        fun withNotifyExecutor(executor: Executor): StageFinalSingle<T>
    }

    /**
     * Builder stage that allows to set initial configuration.
     *
     * @param T type of data
     */
    interface StageFinalSingle<T> {

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Map<String, Any?>): StageFinalSingle<T>

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Array<Pair<String, Any?>>): StageFinalSingle<T> = withInitialConfig(initialConfig.toMap())

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Iterable<Pair<String, Any?>>): StageFinalSingle<T> = withInitialConfig(initialConfig.toMap())

        /**
         * Set flag to indicate that data must be fetched instantly after [ConfigLiveData] object instantiated.
         *
         * @param observeInstantly `true` to fetch data instantly, `false` otherwise
         *
         * @return same builder for chaining calls
         */
        fun withObserveInstantly(observeInstantly: Boolean): StageFinalSingle<T>

        /**
         * Create a new instance of [ConfigLiveData].
         *
         * @return a new instance of [ConfigLiveData]
         */
        fun build(): ConfigLiveData<T?>
    }

    ///
    /// PagedList builder interface
    ///
    /**
     * Builder stage that allows to set data provider.
     *
     * @param K type of key
     * @param V type of value
     */
    interface StageDataSourcePagedList<K, V> {

        /**
         * Set a data source factory that will be used as data provider for [ConfigLiveData].
         *
         * @param factory factory that produces [DataSource]s
         *
         * @return same builder for chaining calls
         */
        fun withDataSourceFactory(factory: DataSource.Factory<K, V>): StageConfigPagedList<K, V> =
                withDataSourceFactory(Factory { factory.create() })

        /**
         * Set a data source factory that will be used as data provider for [ConfigLiveData].
         *
         * @param dataFactory factory that produces [DataSource]s
         *
         * @return same builder for chaining calls
         */
        fun withDataSourceFactory(dataFactory: DataFactory<DataSource<K, V>>): StageConfigPagedList<K, V>

        /**
         * Set a data source factory that will be used as data provider for [ConfigLiveData].
         *
         * @param factory factory that produces [DataSource]s
         *
         * @return same builder for chaining calls
         */
        fun withDataSourceFactory(factory: (Configuration) -> DataSource<K, V>): StageConfigPagedList<K, V> =
                withDataSourceFactory(Factory { factory(it) })

        /**
         * Set a collection that will be converted to [ListDataSource].
         *
         * @param data collection of items
         *
         * @return same builder for chaining calls
         */
        fun withCollection(data: Collection<V>): StageFetchExecutorPagedList<K, V> =
                withDataSourceFactory { listDataSourceOf(data.toList()) as DataSource<K, V> }
                        .withPageSize(data.size)

        /**
         * Set an array that will be converted to [ListDataSource].
         *
         * @param data array of items
         *
         * @return same builder for chaining calls
         */
        fun withArray(data: Array<V>): StageFetchExecutorPagedList<K, V> = withCollection(data.toList())

        /**
         * Set [LiveData] that will be used as data provider for [ConfigLiveData].
         *
         * @param liveData instance of [LiveData]
         *
         * @return same builder for chaining calls
         */
        fun withLiveData(liveData: LiveData<out Collection<V>>): StageConfigPagedList<K, V>
    }

    /**
     * Builder stage that allows to configure [PagedList].
     *
     * @param K type of key
     * @param V type of value
     */
    interface StageConfigPagedList<K, V> {

        /**
         * Set a configuration for [PagedList].
         *
         * @param config instance of `PagedList.Config`
         *
         * @return same builder for chaining calls
         */
        fun withConfig(config: PagedList.Config): StageFetchExecutorPagedList<K, V>

        /**
         * Set a page size for [PagedList].
         *
         * @param pageSize page size
         *
         * @return same builder for chaining calls
         *
         * @see PagedList.Config.Builder.setPageSize
         */
        fun withPageSize(pageSize: Int): StageFetchExecutorPagedList<K, V>
    }

    /**
     * Builder stage that allows to set fetch executor.
     *
     * @param K type of key
     * @param V type of value
     */
    interface StageFetchExecutorPagedList<K, V> {

        /**
         * Set executor that will be used to fetch data from data provider.
         *
         * @param executor instance of [Executor]
         *
         * @return same builder for chaining calls
         */
        fun withFetchExecutor(executor: Executor): StageNotifyExecutorPagedList<K, V>
    }

    /**
     * Builder stage that allows to set notify executor.
     *
     * @param K type of key
     * @param V type of value
     */
    interface StageNotifyExecutorPagedList<K, V> {

        /**
         * Set executor that will be used to notify data loaded.
         *
         * @param executor instance of [Executor]
         *
         * @return same builder for chaining calls
         */
        fun withNotifyExecutor(executor: Executor): StageFinalPagedList<K, V>
    }

    /**
     * Builder stage that allows to set initial configuration.
     *
     * @param K type of key
     * @param V type of value
     */
    interface StageFinalPagedList<K, V> {

        /**
         * Set the initial key the DataSource should load around as part of initialization.
         *
         * @param initialKey initial key
         *
         * @return same builder for chaining calls
         *
         * @see PagedList.Builder.setInitialKey
         */
        fun withInitialKey(initialKey: K): StageFinalPagedList<K, V>

        /**
         * Set boundary callback for loading data.
         *
         * @param boundaryCallback boundary callback
         *
         * @return same builder for chaining calls
         *
         * @see PagedList.Builder.setBoundaryCallback
         */
        fun withBoundaryCallback(boundaryCallback: PagedList.BoundaryCallback<V>): StageFinalPagedList<K, V>

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Map<String, Any?>): StageFinalPagedList<K, V>

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Array<Pair<String, Any?>>): StageFinalPagedList<K, V> = withInitialConfig(initialConfig.toMap())

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Iterable<Pair<String, Any?>>): StageFinalPagedList<K, V> = withInitialConfig(initialConfig.toMap())

        /**
         * Set flag to indicate that data must be fetched instantly after [ConfigLiveData] object instantiated.
         *
         * @param observeInstantly `true` to fetch data instantly, `false` otherwise
         *
         * @return same builder for chaining calls
         */
        fun withObserveInstantly(observeInstantly: Boolean): StageFinalPagedList<K, V>

        /**
         * Create a new instance of [ConfigLiveData].
         *
         * @return a new instance of [ConfigLiveData]
         */
        fun build(): ConfigLiveData<PagedList<V>?>
    }
}


/**
 * Create a new [OnInvalidatedObserver] that wraps passed [block].
 *
 * @param block block that will be executed when [ConfigLiveData] invalidates.
 *
 * @return a new instance of `OnInvalidatedObserver`
 */
inline fun OnInvalidatedObserver(crossinline block: () -> Unit) = object : ConfigLiveData.OnInvalidatedObserver {
    override fun onLiveDataInvalidated() = block()
}

/**
 * Create a new [ConfigLiveData.DataFactory] that wraps passed [block].
 *
 * @param block block that will be executed when [ConfigLiveData.DataFactory.create] method called.
 *
 * @return a new instance of `ConfigLiveData.Factory`
 */
inline fun <T> Factory(crossinline block: (Configuration) -> T) = object : ConfigLiveData.DataFactory<T> {
    override fun create(configuration: Configuration): T = block(configuration)
}

/**
 * Create a new [ConfigLiveData.DataMapper] that wraps passed [block].
 *
 * @param block block that will be executed when value must be converted to a new type
 *
 * @return a new instance of `ConfigLiveData.DataMapper`
 */
inline fun <T, R> Mapper(crossinline block: (T) -> R) = object : ConfigLiveData.DataMapper<T, R> {
    override fun map(value: T): R {
        return block(value)
    }
}