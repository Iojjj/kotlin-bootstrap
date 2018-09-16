package com.github.iojjj.bootstrap.adapters.data.providers

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import com.github.iojjj.bootstrap.adapters.data.ConfigLiveData
import com.github.iojjj.bootstrap.adapters.data.Configuration
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf

/**
 * Implementation of [LiveDataProvider] that uses [ConfigLiveData.DataFactory] to compute values.
 *
 * @param T type of data
 */
@SuppressLint("RestrictedApi")
internal class DataFactoryProvider<T>(private val delegate: ConfigLiveData.DataFactory<T>,
                                      observable: Observable<() -> Unit> = observableOf())
    :
        LiveDataProvider<T>,
        Observable<() -> Unit> by observable {

    override fun compute(configuration: Configuration) = delegate.create(configuration)

    override fun observe(owner: LifecycleOwner) {
        // nothing to observe. Factory can only produce values when ConfigLiveData will call it's `compute` method
    }
}