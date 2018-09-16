package com.github.iojjj.bootstrap.adapters.data.providers

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.arch.paging.DataSource
import android.arch.paging.PagedList
import com.github.iojjj.bootstrap.adapters.data.Configuration
import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf
import java.util.concurrent.Executor

/**
 * Implementation of [LiveDataProvider] that uses [DataSource] to compute [PagedList] values.
 *
 * @param K type of key
 * @param V type of value
 */
@SuppressLint("RestrictedApi")
internal class PagedListLiveDataProvider<K, V>(private val delegate: LiveDataProvider<DataSource<K, V>>,
                                               private val config: PagedList.Config,
                                               private val fetchExecutor: Executor,
                                               private val notifyExecutor: Executor,
                                               private var initialKey: K?,
                                               private var boundaryCallback: PagedList.BoundaryCallback<V>?,
                                               private val observable: InvokableObservable<() -> Unit> = observableOf())
    :
        LiveDataProvider<PagedList<V>?>,
        Observable<() -> Unit> by observable {

    private var list: PagedList<V>? = null
    private var dataSource: DataSource<K, V>? = null
    private val invalidatedCallback = DataSource.InvalidatedCallback { observable.notifyObservers { it() } }

    init {
        delegate.addObserver { invalidatedCallback.onInvalidated() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun compute(configuration: Configuration): PagedList<V>? {
        val initialKey = list?.lastKey as? K ?: initialKey
        do {
            dataSource?.removeInvalidatedCallback(invalidatedCallback)
            dataSource = delegate.compute(configuration)
            dataSource!!.addInvalidatedCallback(invalidatedCallback)
            list = PagedList.Builder(dataSource!!, config)
                    .setInitialKey(initialKey)
                    .setFetchExecutor(fetchExecutor)
                    .setNotifyExecutor(notifyExecutor)
                    .setBoundaryCallback(boundaryCallback)
                    .build()
        } while (list!!.isDetached)
        return list
    }

    override fun observe(owner: LifecycleOwner) {
        delegate.observe(owner)
    }
}