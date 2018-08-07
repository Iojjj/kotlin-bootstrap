@file:Suppress("FunctionName")

package com.github.iojjj.bootstrap.adapters.data

import android.annotation.SuppressLint
import android.arch.core.executor.ArchTaskExecutor
import android.arch.lifecycle.*
import android.arch.paging.DataSource
import android.arch.paging.PagedList
import android.support.annotation.MainThread
import com.github.iojjj.bootstrap.utils.observableOf
import java.util.concurrent.Executor

/**
 * Implementation of [ComputableLiveData] that has its own [Configuration] object and invalidates every time configuration changes.
 *
 * @param T type of data
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
@SuppressLint("RestrictedApi")
class ConfigLiveData<T> private constructor(private val delegate: LiveDataProvider<T>, observeInstantly: Boolean, fetchExecutor: Executor)
    :
        ComputableLiveData<T?>(fetchExecutor) {

    @Suppress("unused")
    companion object {

        /**
         * Create a new instance of [ConfigLiveData] builder intended to be used with single values.
         *
         * @return a new instance of `ConfigLiveData` builder
         */
        @JvmStatic
        fun <T> ofSingle(): StageDataSourceSingle<T> = SingleBuilder()

        /**
         * Create a new instance of [ConfigLiveData] builder intended to be used with [PagedList]s.
         *
         * @return a new instance of `ConfigLiveData` builder
         */
        @JvmStatic
        fun <K, V> ofPagedList(): StageDataSourcePagedList<K, V> = PagedListBuilder()
    }

    /**
     * Configuration of `this` live data. Any changes to configuration will lead to [invalidate] method be invoked.
     */
    val configuration: Configuration = Configuration()

    /**
     * Observable responsible for notifying about initial load events.
     */
    val onInitialLoadObservable = observableOf<OnInitialLoadObserver<T>>()

    /**
     * Observable responsible for notifying about live data invalidation.
     */
    val onInvalidatedObservable = observableOf<OnInvalidatedObserver>()

    private val configChangedListener = OnConfigurationChangedListener { invalidate() }
    private val instantObserver by lazy { Observer<T?> { } }
    private var data: T? = null

    init {
        configuration.addObserver(configChangedListener)
        delegate.addObserver(this::invalidate)
        if (observeInstantly) {
            // add an empty observer to pre-load data
            liveData.observeForever(instantObserver)
        }
    }

    // overridden to suppress the error
    @Suppress("RedundantOverride")
    override fun getLiveData(): LiveData<T?> {
        return super.getLiveData()
    }

    override fun compute(): T? {
        val notify = data == null
        if (notify) {
            ArchTaskExecutor.getMainThreadExecutor().execute {
                onInitialLoadObservable.notifyObservers { it.onStartLoading() }
            }
        }
        data = delegate.compute(configuration)
        if (notify) {
            ArchTaskExecutor.getMainThreadExecutor().execute {
                onInitialLoadObservable.notifyObservers { it.onStopLoading(data) }
            }
        }
        return data
    }

    override fun invalidate() {
        super.invalidate()
        onInvalidatedObservable.notifyObservers(OnInvalidatedObserver::onLiveDataInvalidated)
    }

    /**
     * Start observing `this` live data using provided [owner]. Observer will be automatically removed with [Lifecycle.Event.ON_DESTROY] event.
     *
     * @param owner instance of [LifecycleOwner]
     * @param observer instance of [Observer] that will receive callbacks from `LiveData`
     */
    fun observe(owner: LifecycleOwner, observer: Observer<T?>) {
        // observe delegate implementation if it supports it
        delegate.observe(owner)
        // observe Android's LiveData
        liveData.observe(owner, observer)
    }

    /**
     * Remove previously registered observer.
     *
     * @param observer instance of [Observer]
     */
    fun removeObserver(observer: Observer<T?>) {
        liveData.removeObserver(observer)
    }

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
    @Suppress("unused")
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
    interface Factory<T> {

        /**
         * Create a new value using provided [configuration].
         *
         * @param configuration configuration object
         *
         * @return a new value
         */
        fun create(configuration: Configuration): T
    }


    ///
    /// Single builder interface
    ///


    @Suppress("unused")
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
        fun withJust(data: T?): StageExecutorSingle<T>

        /**
         * Set a live data object that will be used as data provider for [ConfigLiveData].
         *
         * @param liveData live data that produces values
         *
         * @return same builder for chaining calls
         */
        fun withLiveData(liveData: LiveData<T?>): StageExecutorSingle<T>

        /**
         * Set a factory that will be used as data provider for [ConfigLiveData].
         *
         * @param factory factory that produces values
         *
         * @return same builder for chaining calls
         */
        fun withFactory(factory: Factory<T?>): StageExecutorSingle<T>

        /**
         * Set a factory that will be used as data provider for [ConfigLiveData].
         *
         * @param factory factory that produces values
         *
         * @return same builder for chaining calls
         */
        fun withFactory(factory: (Configuration) -> T?): StageExecutorSingle<T> = withFactory(Factory { factory(it) })

    }

    /**
     * Builder stage that allows to set fetch executor.
     *
     * @param T type of data
     */
    interface StageExecutorSingle<T> {

        /**
         * Set executor that will be used to fetch data from data provider.
         *
         * @param executor instance of [Executor]
         *
         * @return same builder for chaining calls
         */
        fun withFetchExecutor(executor: Executor): StageFinalSingle<T>
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
    /// Single builder implementation
    ///

    private class SingleBuilder<T>
        :
            StageDataSourceSingle<T>,
            StageExecutorSingle<T>,
            StageFinalSingle<T> {

        private lateinit var delegate: LiveDataProvider<T?>
        private lateinit var fetchExecutor: Executor
        private var observeInstantly = false
        private var initialConfig: Map<String, Any?>? = null

        override fun withJust(data: T?): StageExecutorSingle<T> {
            this.delegate = SingleItemLiveDataProvider(data)
            return this
        }

        override fun withLiveData(liveData: LiveData<T?>): StageExecutorSingle<T> {
            this.delegate = liveData.toLiveDataProvider()
            return this
        }

        override fun withFactory(factory: Factory<T?>): StageExecutorSingle<T> {
            this.delegate = factory.toLiveDataProvider()
            return this
        }

        override fun withFetchExecutor(executor: Executor): StageFinalSingle<T> {
            this.fetchExecutor = executor
            return this
        }

        override fun withObserveInstantly(observeInstantly: Boolean): StageFinalSingle<T> {
            this.observeInstantly = observeInstantly
            return this
        }

        override fun withInitialConfig(initialConfig: Map<String, Any?>): StageFinalSingle<T> {
            this.initialConfig = initialConfig
            return this
        }

        override fun build(): ConfigLiveData<T?> {
            return ConfigLiveData(delegate, observeInstantly, fetchExecutor).apply {
                initialConfig?.let(configuration::load)
            }
        }
    }

    ///
    /// PagedList builder interface
    ///

    @Suppress("unused", "UNCHECKED_CAST")
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
         * @param factory factory that produces [DataSource]s
         *
         * @return same builder for chaining calls
         */
        fun withDataSourceFactory(factory: Factory<DataSource<K, V>>): StageConfigPagedList<K, V>

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
        fun withCollection(data: Collection<V>): StageExecutorPagedList<K, V> =
                withDataSourceFactory { listDataSourceOf(data.toList()) as DataSource<K, V> }
                        .withPageSize(data.size)

        /**
         * Set an array that will be converted to [ListDataSource].
         *
         * @param data array of items
         *
         * @return same builder for chaining calls
         */
        fun withArray(data: Array<V>): StageExecutorPagedList<K, V> = withCollection(data.toList())

        fun withLiveData(liveData: LiveData<out Collection<V>>): StageConfigPagedList<K, V> = withLiveData(liveData) { it }

        fun <T> withLiveData(liveData: LiveData<out Collection<V>>, mapper: (Collection<V>) -> Collection<T>): StageConfigPagedList<K, T>
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
        fun withConfig(config: PagedList.Config): StageExecutorPagedList<K, V>

        /**
         * Set a page size for [PagedList].
         *
         * @param pageSize page size
         *
         * @return same builder for chaining calls
         *
         * @see PagedList.Config.Builder.setPageSize
         */
        fun withPageSize(pageSize: Int): StageExecutorPagedList<K, V>
    }

    /**
     * Builder stage that allows to set fetch executor.
     *
     * @param K type of key
     * @param V type of value
     */
    interface StageExecutorPagedList<K, V> {

        /**
         * Set executor that will be used to fetch data from data provider.
         *
         * @param executor instance of [Executor]
         *
         * @return same builder for chaining calls
         */
        fun withFetchExecutor(executor: Executor): StageFinalPagedList<K, V>
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

    ///
    /// PagedList builder implementation
    ///

    private class PagedListBuilder<K, V>
        :
            StageDataSourcePagedList<K, V>,
            StageExecutorPagedList<K, V>,
            StageConfigPagedList<K, V>,
            StageFinalPagedList<K, V> {

        private lateinit var delegate: LiveDataProvider<DataSource<K, V>>
        private lateinit var config: PagedList.Config
        private lateinit var fetchExecutor: Executor
        private var initialKey: K? = null
        private var initialConfig: Map<String, Any?>? = null
        private var boundaryCallback: PagedList.BoundaryCallback<V>? = null
        private var observeInstantly = false
        private var liveData: LiveData<out Collection<V>>? = null

        override fun withDataSourceFactory(factory: Factory<DataSource<K, V>>): StageConfigPagedList<K, V> {
            this.delegate = factory.toLiveDataProvider()
            return this
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T> withLiveData(liveData: LiveData<out Collection<V>>, mapper: (Collection<V>) -> Collection<T>): StageConfigPagedList<K, T> {
            this.delegate = liveData.toLiveDataProvider().map(mapper).map { ListDataSource.of(it) as DataSource<K, V> }
            return this as StageConfigPagedList<K, T>
        }

        override fun withConfig(config: PagedList.Config): StageExecutorPagedList<K, V> {
            this.config = config
            return this
        }

        override fun withPageSize(pageSize: Int): StageExecutorPagedList<K, V> {
            this.config = PagedList.Config.Builder()
                    .setPageSize(pageSize)
                    .build()
            return this
        }

        override fun withFetchExecutor(executor: Executor): StageFinalPagedList<K, V> {
            this.fetchExecutor = executor
            return this
        }

        override fun withInitialKey(initialKey: K): StageFinalPagedList<K, V> {
            this.initialKey = initialKey
            return this
        }

        override fun withInitialConfig(initialConfig: Map<String, Any?>): StageFinalPagedList<K, V> {
            this.initialConfig = initialConfig
            return this
        }

        override fun withBoundaryCallback(boundaryCallback: PagedList.BoundaryCallback<V>): StageFinalPagedList<K, V> {
            this.boundaryCallback = boundaryCallback
            return this
        }

        override fun withObserveInstantly(observeInstantly: Boolean): PagedListBuilder<K, V> {
            this.observeInstantly = observeInstantly
            return this
        }

        override fun build(): ConfigLiveData<PagedList<V>?> {
            val delegate = PagedListLiveDataProvider(delegate,
                    config,
                    fetchExecutor,
                    ArchTaskExecutor.getMainThreadExecutor(),
                    initialKey,
                    boundaryCallback)
            return ConfigLiveData(delegate, observeInstantly, fetchExecutor).apply {
                initialConfig?.let(configuration::load)
            }
        }
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
 * Create a new [ConfigLiveData.Factory] that wraps passed [block].
 *
 * @param block block that will be executed when [ConfigLiveData.Factory.create] method called.
 *
 * @return a new instance of `ConfigLiveData.Factory`
 */
inline fun <T> Factory(crossinline block: (Configuration) -> T) = object : ConfigLiveData.Factory<T> {
    override fun create(configuration: Configuration): T = block(configuration)
}