package com.github.iojjj.bootstrap.adapters.data

import android.arch.paging.DataSource

/**
 * Implementation of [ConfigurableLiveData.Factory] that wraps [DataSource.Factory].
 */
private class FactoryWrapper<K, V>(private val delegate: DataSource.Factory<K, V>) :
        ConfigurableLiveData.Factory<DataSource<K, V>> {

    override fun create(configuration: Configuration): DataSource<K, V> = delegate.create()
}

/**
 * Convert `this` factory to [ConfigurableLiveData.Factory].
 */
fun <K, V> DataSource.Factory<K, V>.toConfigurableFactory(): ConfigurableLiveData.Factory<DataSource<K, V>> {
    return FactoryWrapper(this)
}