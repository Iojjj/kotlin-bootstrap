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
@SuppressLint("RestrictedApi")
class ConfigurableLiveData<T> private constructor(private val delegate: ConfigurableComputableLiveData<T>) : ComputableLiveData<T>() {

    companion object {

        // TODO: check if it's working, than mark JvmStatic
        fun <T> of(data: T): ConfigurableLiveData<T> {
            return ConfigurableLiveData(FactoryLiveData(object : Factory<T> {
                override fun create(configuration: Configuration): T {
                    return data
                }
            }))
        }

        // TODO: check if it's working, than mark JvmStatic
        fun <T> of(liveData: LiveData<T>): ConfigurableLiveData<T> = ConfigurableLiveData(liveData.toConfigurableLiveData())

        // TODO: check if it's working, than mark JvmStatic
        fun <T> of(factory: Factory<T>): ConfigurableLiveData<T> = ConfigurableLiveData(factory.toConfigurableLiveData())

        /**
         * Create a new instance of [ConfigurableLiveData] that will produce [PagedList] filled with items from [data].
         *
         * @param data list of items
         *
         * @return a new instance of `ConfigurableLiveData`
         */
        @JvmStatic
        fun <T> ofPagedList(data: List<T>): ConfigurableLiveData<PagedList<T>> {
            return ConfigurableLiveData(FactoryLiveData(object : Factory<PagedList<T>> {
                override fun create(configuration: Configuration): PagedList<T> {
                    val config = PagedList.Config.Builder()
                            .setPageSize(data.size)
                            .setInitialLoadSizeHint(data.size)
                            .setEnablePlaceholders(false)
                            .build()
                    return PagedList.Builder(listDataSourceOf(data), config)
                            .setFetchExecutor(ArchTaskExecutor.getMainThreadExecutor())
                            .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                            .build()
                }
            }))
        }

        /**
         * Create a new instance of [ConfigurableLiveData] that will produce [PagedList] filled with items from [data].
         *
         * @param data array of items
         *
         * @return a new instance of `ConfigurableLiveData`
         */
        @JvmStatic
        fun <T> ofPagedList(data: Array<T>): ConfigurableLiveData<PagedList<T>> = ofPagedList(data.asList())

        /**
         * Create a new instance of [ConfigurableLiveData] that will produce [PagedList]s.
         *
         * @return a new instance of `ConfigurableLiveData`
         */
        @JvmStatic
        fun <K, V> ofPagedList(): BuilderStepDataSource<K, V> = Builder()
    }

    val configuration: Configuration = Configuration()

    @Suppress("RedundantOverride")
    override fun getLiveData(): LiveData<T> {
        return super.getLiveData()
    }

    private val onFirstTimeLoadingObservable = observableOf<OnFirstTimeLoadingObserver<T>>()
    private val onInvalidatedObservable = observableOf<OnInvalidatedObserver>()
    private val configurationChangedListener = object : Configuration.OnConfigurationChangedListener {
        override fun onConfigurationChanged(configuration: Configuration) {
            invalidate()
        }
    }

    init {
        configuration.addObserver(configurationChangedListener)
        delegate.addObserver(this::invalidate)
    }

    /**
     * Start observing `LiveData` using provided `LifecycleOwner`. Observing will automatically stopped with [Lifecycle.Event.ON_DESTROY] event.
     *
     * @param owner instance of [LifecycleOwner]
     * @param observer instance of [Observer] that will receive callbacks from `LiveData`
     */
    fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        // observe Android's LiveData
        liveData.observe(owner, observer)
        // observe inner LiveData implementation if it supports it
        delegate.observe(owner)
    }

    /**
     * Add an observer to be called when this [ConfigurableLiveData] loads data for the first time.
     *
     * @param observer instance of [OnFirstTimeLoadingObserver]
     *
     * @return `true` if observer added, `false` otherwise
     */
    fun addOnFirstTimeLoadingObserver(observer: OnFirstTimeLoadingObserver<T>): Boolean = onFirstTimeLoadingObservable.addObserver(observer)

    /**
     * Remove a previously added observer.
     *
     * @param observer instance of [OnFirstTimeLoadingObserver]
     *
     * @return `true` if observer removed, `false` otherwise
     */
    fun removeOnFirstTimeLoadingObserver(observer: OnFirstTimeLoadingObserver<T>): Boolean = onFirstTimeLoadingObservable.removeObserver(observer)

    /**
     * Remove all observers.
     */
    fun clearOnFirstTimeLoadingObservers() = onFirstTimeLoadingObservable.clearObservers()

    /**
     * Add an observer to be called when this [ConfigurableLiveData] is invalidated.
     *
     * @param observer instance of [OnInvalidatedObserver]
     *
     * @return `true` if observer added, `false` otherwise
     */
    fun addOnInvalidatedObserver(observer: OnInvalidatedObserver): Boolean = onInvalidatedObservable.addObserver(observer)

    /**
     * Remove a previously added observer.
     *
     * @param observer instance of [OnInvalidatedObserver]
     *
     * @return `true` if observer removed, `false` otherwise
     */
    fun removeOnInvalidatedObserver(observer: OnInvalidatedObserver): Boolean = onInvalidatedObservable.removeObserver(observer)

    /**
     * Remove all observers.
     */
    fun clearOnInvalidatedObservers() = onInvalidatedObservable.clearObservers()

    override fun compute(): T {
        return delegate.compute(configuration)
    }

    override fun invalidate() {
        onInvalidatedObservable.notifyObservers(OnInvalidatedObserver::onLiveDataInvalidated)
        super.invalidate()
    }

    /**
     * Observer that called when [ConfigurableLiveData] loads data for the first time.
     */
    interface OnFirstTimeLoadingObserver<T> {

        /**
         * Called when [ConfigurableLiveData] starts loading of the data.
         */
        @MainThread
        fun onStartLoading()

        /**
         * Called when [ConfigurableLiveData] stops loading of the data.
         *
         * @param pagedList loaded `PagedList` or `null`
         */
        @MainThread
        fun onStopLoading(pagedList: T?)
    }

    /**
     * Empty implementation of [OnFirstTimeLoadingObserver].
     */
    abstract class OnFirstTimeLoadingAdapter<T> : OnFirstTimeLoadingObserver<T> {

        override fun onStartLoading() {
            /* no-op */
        }

        override fun onStopLoading(pagedList: T?) {
            /* no-op */
        }

    }

    /**
     * Observer that called when [ConfigurableLiveData] is invalidated.
     */
    @FunctionalInterface
    interface OnInvalidatedObserver {

        /**
         * Called when [ConfigurableLiveData] is invalidated.
         */
        fun onLiveDataInvalidated()
    }

    /**
     * Factory used to produce values.
     */
    @FunctionalInterface
    interface Factory<T> {

        /**
         * Create a new value using provided [configuration].
         *
         * @param configuration configuration object with some data
         *
         * @return a new value
         */
        fun create(configuration: Configuration): T
    }

    /**
     * First stage of building a [ConfigurableLiveData].
     */
    interface BuilderStepDataSource<K, V> {

        /**
         * Set data source factory.
         *
         * @param factory instance of `ConfigurableDataSourceFactory`
         *
         * @return next stage of builder
         */
        fun withDataSourceFactory(factory: Factory<DataSource<K, V>>): BuilderStepPagedList<K, V>

        /**
         * Set data source factory.
         *
         * @param factory instance of `DataSource.Factory`
         *
         * @return next stage of builder
         */
        fun withDataSourceFactory(factory: DataSource.Factory<K, V>): BuilderStepPagedList<K, V>
    }

    /**
     * Second stage of building a [ConfigurableLiveData].
     */
    interface BuilderStepPagedList<K, V> {

        /**
         * Set `PagedList`'s configuration.
         *
         * @param config instance of `PagedList.Config`
         *
         * @return next stage of builder
         */
        fun withPagedListConfig(config: PagedList.Config): BuilderStepExecutor<K, V>

        /**
         * Set `PagedList`'s page size.
         *
         * @param pageSize page size
         *
         * @return next stage of builder
         *
         * @see PagedList.Config.Builder.setPageSize
         */
        fun withPageSize(pageSize: Int): BuilderStepExecutor<K, V>
    }

    /**
     * Third stage of building a [ConfigurableLiveData].
     */
    interface BuilderStepExecutor<K, V> {

        /**
         * Set executor that will run any task on background thread.
         *
         * @param executor instance of `Executor`
         *
         * @return next stage of builder
         *
         * @see PagedList.Builder.setFetchExecutor
         */
        fun withFetchExecutor(executor: Executor): BuilderStepFinal<K, V>
    }

    /**
     * Final stage of building a [ConfigurableLiveData].
     */
    interface BuilderStepFinal<K, V> {

        /**
         * Set initial key for loading data.
         *
         * @param initialKey initial key
         *
         * @return same builder for chaining calls
         *
         * @see PagedList.Builder.setInitialKey
         */
        fun withInitialKey(initialKey: K): BuilderStepFinal<K, V>

        /**
         * Set boundary callback for loading data.
         *
         * @param boundaryCallback boundary callback
         *
         * @return same builder for chaining calls
         *
         * @see PagedList.Builder.setBoundaryCallback
         */
        fun withBoundaryCallback(boundaryCallback: PagedList.BoundaryCallback<V>): BuilderStepFinal<K, V>

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Map<String, Any?>): BuilderStepFinal<K, V>

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Array<Pair<String, Any?>>): BuilderStepFinal<K, V>

        /**
         * Set key/value pairs that will pre-fill [Configuration]. Subsequent calls will erase previous data.
         *
         * @param initialConfig map that holds key/value pairs
         *
         * @return same builder for chaining calls
         */
        fun withInitialConfig(initialConfig: Iterable<Pair<String, Any?>>): BuilderStepFinal<K, V>

        /**
         * Create a new instance of [ConfigurableLiveData].
         *
         * @return a new instance of [ConfigurableLiveData]
         */
        fun build(): ConfigurableLiveData<PagedList<V>>
    }

    private class Builder<K, V> : BuilderStepDataSource<K, V>,
            BuilderStepExecutor<K, V>,
            BuilderStepPagedList<K, V>,
            BuilderStepFinal<K, V> {

        private lateinit var factory: Factory<DataSource<K, V>>
        private lateinit var config: PagedList.Config
        private lateinit var fetchExecutor: Executor
        private var initialKey: K? = null
        private var initialConfig: Map<String, Any?>? = null
        private var boundaryCallback: PagedList.BoundaryCallback<V>? = null

        override fun withDataSourceFactory(factory: Factory<DataSource<K, V>>): BuilderStepPagedList<K, V> {
            this.factory = factory
            return this
        }

        override fun withDataSourceFactory(factory: DataSource.Factory<K, V>): BuilderStepPagedList<K, V> =
                withDataSourceFactory(factory.toConfigurableFactory())

        override fun withPagedListConfig(config: PagedList.Config): BuilderStepExecutor<K, V> {
            this.config = config
            return this
        }

        override fun withPageSize(pageSize: Int): BuilderStepExecutor<K, V> {
            this.config = PagedList.Config.Builder()
                    .setPageSize(pageSize)
                    .build()
            return this
        }

        override fun withFetchExecutor(executor: Executor): BuilderStepFinal<K, V> {
            this.fetchExecutor = executor
            return this
        }

        override fun withInitialKey(initialKey: K): BuilderStepFinal<K, V> {
            this.initialKey = initialKey
            return this
        }

        override fun withInitialConfig(initialConfig: Map<String, Any?>): BuilderStepFinal<K, V> {
            this.initialConfig = initialConfig
            return this
        }

        override fun withInitialConfig(initialConfig: Array<Pair<String, Any?>>): BuilderStepFinal<K, V> = withInitialConfig(initialConfig.toMap())

        override fun withInitialConfig(initialConfig: Iterable<Pair<String, Any?>>): BuilderStepFinal<K, V> = withInitialConfig(initialConfig.toMap())

        override fun withBoundaryCallback(boundaryCallback: PagedList.BoundaryCallback<V>): BuilderStepFinal<K, V> {
            this.boundaryCallback = boundaryCallback
            return this
        }

        override fun build(): ConfigurableLiveData<PagedList<V>> {
            val delegate = PagedListLiveData(factory,
                    config,
                    fetchExecutor,
                    ArchTaskExecutor.getMainThreadExecutor(),
                    initialKey,
                    boundaryCallback)
            return ConfigurableLiveData(delegate).apply {
                initialConfig?.let(configuration::load)
            }
        }
    }
}

/**
 * Create a new [OnInvalidatedObserver] that wraps passed [block].
 *
 * @param block block that will be executed when [ConfigurableLiveData] invalidates.
 *
 * @return a new instance of `OnInvalidatedObserver`
 */
@Suppress("FunctionName")
inline fun OnInvalidatedObserver(crossinline block: () -> Unit) = object : ConfigurableLiveData.OnInvalidatedObserver {
    override fun onLiveDataInvalidated() = block()
}