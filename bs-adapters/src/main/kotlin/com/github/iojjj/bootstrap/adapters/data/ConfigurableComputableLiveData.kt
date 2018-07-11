package com.github.iojjj.bootstrap.adapters.data

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import com.github.iojjj.bootstrap.utils.Observable

/**
 * Base interface of `ComputableLiveData` that computes values when asked by their holders.
 */
internal interface ConfigurableComputableLiveData<T> : Observable<() -> Unit> {

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

internal fun <T> LiveData<T>.toConfigurableLiveData(): ConfigurableComputableLiveData<T> {
    return LiveDataWrapper(this)
}

internal fun <T> ConfigurableLiveData.Factory<T>.toConfigurableLiveData(): ConfigurableComputableLiveData<T> {
    return FactoryLiveData(this)
}