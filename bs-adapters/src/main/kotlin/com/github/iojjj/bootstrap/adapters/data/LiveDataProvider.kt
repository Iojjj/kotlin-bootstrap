package com.github.iojjj.bootstrap.adapters.data

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import com.github.iojjj.bootstrap.utils.Observable

/**
 * Base interface of `ComputableLiveData` that computes values when asked by their holders.
 *
 * @param T type of data
 */
internal interface LiveDataProvider<T> : Observable<() -> Unit> {

    /**
     * Compute value using provided [configuration].
     *
     * @param configuration configuration object with some data
     *
     * @return computed value
     */
    fun compute(configuration: Configuration): T

    /**
     * Start observing lifecycle changes if required by implementation.
     *
     * @param owner instance of [LifecycleOwner]
     */
    fun observe(owner: LifecycleOwner)
}

/**
 * Wrap [LiveData] with [LiveDataWrapper].
 *
 * @receiver instance of [LiveData]
 *
 * @return a new instance of [LiveDataWrapper]
 */
internal fun <T> LiveData<T>.toLiveDataProvider(): LiveDataWrapper<T> {
    return LiveDataWrapper(this)
}

/**
 * Wrap [ConfigLiveData.Factory] with [FactoryLiveDataProvider].
 *
 * @receiver instance of [ConfigLiveData.Factory]
 *
 * @return a new instance of [FactoryLiveDataProvider]
 */
internal fun <T> ConfigLiveData.Factory<T>.toLiveDataProvider(): FactoryLiveDataProvider<T> {
    return FactoryLiveDataProvider(this)
}

internal fun <T, V> LiveDataProvider<T>.map(mapper: (T) -> V): LiveDataProvider<V> {
    return LiveDataMapper(this, mapper)
}