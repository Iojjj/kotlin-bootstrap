package com.github.iojjj.bootstrap.adapters.data

import android.arch.lifecycle.LifecycleOwner
import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf

/**
 * Implementation of [LiveDataProvider] that uses provided [data] as a return value.
 *
 * @param T type of data
 */
internal class SingleItemLiveDataProvider<T>(private val data: T,
                                             observable: InvokableObservable<() -> Unit> = observableOf())
    :
        LiveDataProvider<T>,
        Observable<() -> Unit> by observable {

    override fun compute(configuration: Configuration): T = data

    override fun observe(owner: LifecycleOwner) {
        /* no-op */
    }

}