package com.github.iojjj.bootstrap.adapters.data

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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

    private val lock = ReentrantLock(true)
    private val condition = lock.newCondition()

    override fun compute(configuration: Configuration): T {
        lock.withLock {
            // wait while value is not produced by delegate yet
            while (delegate.value == null) {
                condition.await()
            }
            return delegate.value!!
        }
    }

    override fun observe(owner: LifecycleOwner) {
        // observe Android's LiveData object and send invalidation callbacks when it loads a new value
        delegate.observe(owner, Observer {
            lock.withLock {
                condition.signal()
            }
            observable.notifyObservers { o -> o() }
        })
    }

}