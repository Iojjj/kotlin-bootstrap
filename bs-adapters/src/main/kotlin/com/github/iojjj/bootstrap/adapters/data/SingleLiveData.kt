package com.github.iojjj.bootstrap.adapters.data

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import com.github.iojjj.bootstrap.adapters.data.providers.LiveDataProvider
import com.github.iojjj.bootstrap.adapters.data.providers.SingleItemLiveDataProvider
import com.github.iojjj.bootstrap.adapters.data.providers.map
import com.github.iojjj.bootstrap.adapters.data.providers.toLiveDataProvider
import com.github.iojjj.bootstrap.utils.observableOf
import java.util.concurrent.Executor

/**
 * Implementation of [ComputableLiveData] that has its own [Configuration] object and invalidates every time configuration changes.
 *
 * @param T type of data
 */
@SuppressLint("RestrictedApi")
internal class SingleLiveData<T> constructor(private val delegate: LiveDataProvider<T>,
                                             private val observeInstantly: Boolean,
                                             private val fetchExecutor: Executor,
                                             private val notifyExecutor: Executor)
    :
        ComputableLiveData<T?>(fetchExecutor), ConfigLiveData<T> {

    companion object {

        /**
         * Create a new instance of [SingleLiveData] builder intended to be used with single values.
         *
         * @return a new instance of `SingleLiveData` builder
         */
        fun <T> newBuilder(): ConfigLiveData.StageDataSourceSingle<T> = SingleBuilder()
    }

    override val configuration: Configuration = Configuration()
    override val onInitialLoadObservable = observableOf<ConfigLiveData.OnInitialLoadObserver<T>>()
    override val onInvalidatedObservable = observableOf<ConfigLiveData.OnInvalidatedObserver>()

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
            notifyExecutor.execute {
                onInitialLoadObservable.notifyObservers { it.onStartLoading() }
            }
        }
        data = delegate.compute(configuration)
        if (notify) {
            notifyExecutor.execute {
                onInitialLoadObservable.notifyObservers { it.onStopLoading(data) }
            }
        }
        return data
    }

    override fun invalidate() {
        super.invalidate()
        onInvalidatedObservable.notifyObservers(ConfigLiveData.OnInvalidatedObserver::onLiveDataInvalidated)
    }

    /**
     * Start observing `this` live data using provided [owner]. Observer will be automatically removed with [Lifecycle.Event.ON_DESTROY] event.
     *
     * @param owner instance of [LifecycleOwner]
     * @param observer instance of [Observer] that will receive callbacks from `LiveData`
     */
    override fun observe(owner: LifecycleOwner, observer: Observer<T?>) {
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
    override fun removeObserver(observer: Observer<T?>) {
        liveData.removeObserver(observer)
    }

    override fun <R> map(mapper: ConfigLiveData.DataMapper<T, R>): ConfigLiveData<R> {
        return SingleLiveData(delegate.map(mapper::map), observeInstantly, fetchExecutor, notifyExecutor)
    }


    ///
    /// Single builder implementation
    ///

    private class SingleBuilder<T>
        :
            ConfigLiveData.StageDataSourceSingle<T>,
            ConfigLiveData.StageFetchExecutorSingle<T>,
            ConfigLiveData.StageNotifyExecutorSingle<T>,
            ConfigLiveData.StageFinalSingle<T> {

        private lateinit var delegate: LiveDataProvider<T?>
        private lateinit var fetchExecutor: Executor
        private lateinit var notifyExecutor: Executor
        private var observeInstantly = false
        private var initialConfig: Map<String, Any?>? = null

        override fun withJust(data: T?): ConfigLiveData.StageFetchExecutorSingle<T> {
            this.delegate = SingleItemLiveDataProvider(data)
            return this
        }

        override fun withLiveData(liveData: LiveData<T?>): ConfigLiveData.StageFetchExecutorSingle<T> {
            this.delegate = liveData.toLiveDataProvider()
            return this
        }

        override fun withDataFactory(dataFactory: ConfigLiveData.DataFactory<T?>): ConfigLiveData.StageFetchExecutorSingle<T> {
            this.delegate = dataFactory.toLiveDataProvider()
            return this
        }

        override fun withFetchExecutor(executor: Executor): ConfigLiveData.StageNotifyExecutorSingle<T> {
            this.fetchExecutor = executor
            return this
        }

        override fun withNotifyExecutor(executor: Executor): ConfigLiveData.StageFinalSingle<T> {
            this.notifyExecutor = executor
            return this
        }

        override fun withObserveInstantly(observeInstantly: Boolean): ConfigLiveData.StageFinalSingle<T> {
            this.observeInstantly = observeInstantly
            return this
        }

        override fun withInitialConfig(initialConfig: Map<String, Any?>): ConfigLiveData.StageFinalSingle<T> {
            this.initialConfig = initialConfig
            return this
        }

        override fun build(): SingleLiveData<T?> {
            return SingleLiveData(delegate, observeInstantly, fetchExecutor, notifyExecutor).apply {
                initialConfig?.let(configuration::load)
            }
        }
    }
}