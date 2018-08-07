package com.github.iojjj.bootstrap.adapters.data

import android.arch.lifecycle.LifecycleOwner

/**
 * Implementation of [LiveDataProvider] that maps computed value using [mapper].
 *
 * @param T consumed type
 * @param V produced type
 *
 * @property delegate delegate used to produce values
 * @property mapper mapper used to map produced values
 */
internal class LiveDataMapper<T, V>(private val delegate: LiveDataProvider<T>,
                                    private val mapper: (T) -> V) : LiveDataProvider<V> {

    override fun compute(configuration: Configuration): V = mapper(delegate.compute(configuration))

    override fun observe(owner: LifecycleOwner) = delegate.observe(owner)

    override fun addObserver(observer: () -> Unit): Boolean = delegate.addObserver(observer)

    override fun removeObserver(observer: () -> Unit): Boolean = delegate.removeObserver(observer)

    override fun clearObservers() = delegate.clearObservers()

}