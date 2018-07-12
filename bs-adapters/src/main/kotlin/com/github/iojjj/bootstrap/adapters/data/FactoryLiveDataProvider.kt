package com.github.iojjj.bootstrap.adapters.data

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf

/**
 * Implementation of [LiveDataProvider] that uses [ConfigLiveData.Factory] to compute values.
 *
 * @param T type of data
 */
@SuppressLint("RestrictedApi")
internal class FactoryLiveDataProvider<T>(private val delegate: ConfigLiveData.Factory<T>,
                                          observable: InvokableObservable<() -> Unit> = observableOf())
    :
        LiveDataProvider<T>,
        Observable<() -> Unit> by observable {

    override fun compute(configuration: Configuration) = delegate.create(configuration)

    override fun observe(owner: LifecycleOwner) {
        // nothing to observe. Factory can only produce values when ConfigLiveData will call it's `compute` method
    }
}