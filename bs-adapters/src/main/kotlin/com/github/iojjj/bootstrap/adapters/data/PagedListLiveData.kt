package com.github.iojjj.bootstrap.adapters.data

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.arch.paging.DataSource
import android.arch.paging.PagedList
import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf
import java.util.concurrent.Executor

/**
 * Implementation of [ConfigurableComputableLiveData] that uses [DataSource] to compute [PagedList] values.
 */
@SuppressLint("RestrictedApi")
internal class PagedListLiveData<K, V>(private val factory: ConfigurableLiveData.Factory<DataSource<K, V>>,
                                       private val config: PagedList.Config,
                                       private val fetchExecutor: Executor,
                                       private val mainExecutor: Executor,
                                       private var initialKey: K?,
                                       private var boundaryCallback: PagedList.BoundaryCallback<V>?,
                                       private val observable: InvokableObservable<() -> Unit> = observableOf())
    :
        ConfigurableComputableLiveData<PagedList<V>>,
        Observable<() -> Unit> by observable {

    private var list: PagedList<V>? = null
    private var dataSource: DataSource<K, V>? = null
    private val invalidatedCallback = DataSource.InvalidatedCallback { observable.notifyObservers { it() } }

    @Suppress("UNCHECKED_CAST")
    override fun compute(configuration: Configuration): PagedList<V> {
        val initialKey = list?.lastKey as? K ?: initialKey
        do {
            dataSource?.removeInvalidatedCallback(invalidatedCallback)
            dataSource = factory.create(configuration)
            dataSource!!.addInvalidatedCallback(invalidatedCallback)
            list = PagedList.Builder(dataSource!!, config)
                    .setInitialKey(initialKey)
                    .setFetchExecutor(fetchExecutor)
                    .setNotifyExecutor(mainExecutor)
                    .setBoundaryCallback(boundaryCallback)
                    .build()
        } while (list!!.isDetached)
        return list!!
    }

    override fun observe(owner: LifecycleOwner) {
        /* no-op */
    }
}