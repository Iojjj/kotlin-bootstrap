package com.github.iojjj.bootstrap.adapters.data

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf

/**
 * Implementation of [LiveDataProvider] that wraps [LiveData] and uses it to compute values.
 *
 * @param T type of data
 */
internal class LiveDataWrapper<T>(private val delegate: LiveData<T>,
                                  private val observable: InvokableObservable<() -> Unit> = observableOf())
    :
        LiveDataProvider<T>,
        Observable<() -> Unit> by observable {

    private var data: T? = null

    override fun compute(configuration: Configuration): T {
        // this method should always be called when data was already loaded
        // FIXME: potential crash, 99%. This method will be called BEFORE any data will be received
        return data!!
    }

    override fun observe(owner: LifecycleOwner) {
        // observe Android's LiveData object and send invalidation callbacks when it loads a new value
        delegate.observe(owner, Observer {
            data = it
            observable.notifyObservers { it() }
        })
    }

}