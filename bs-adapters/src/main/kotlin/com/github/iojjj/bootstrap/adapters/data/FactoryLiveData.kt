package com.github.iojjj.bootstrap.adapters.data

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf

/**
 * Implementation of [ConfigurableComputableLiveData] that uses [ConfigurableLiveData.Factory] to compute values.
 */
@SuppressLint("RestrictedApi")
internal class FactoryLiveData<T>(private val factory: ConfigurableLiveData.Factory<T>,
                                  private val observable: InvokableObservable<() -> Unit> = observableOf())
    :
        ConfigurableComputableLiveData<T>,
        Observable<() -> Unit> by observable {

    override fun compute(configuration: Configuration) = factory.create(configuration)

    override fun observe(owner: LifecycleOwner) {
        // nothing to observe. Factory can only produce values when ConfigurableLiveData will call it's `compute` method
    }
}