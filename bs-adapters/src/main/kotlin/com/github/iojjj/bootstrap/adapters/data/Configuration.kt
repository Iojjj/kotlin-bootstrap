package com.github.iojjj.bootstrap.adapters.data

import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf

/**
 * Wrapper over [Map] that notifies observers when it changed.
 *
 * @property notifyConfigurationChanges Flag indicates that observers should be notified once this configuration changed.
 * @property entries `Set` of all key/value pairs in this configuration.
 */
class Configuration internal constructor(
        private val observable: InvokableObservable<OnConfigurationChangedListener> = observableOf())
    :
        Observable<Configuration.OnConfigurationChangedListener> by observable {

    private val delegate by lazy { HashMap<String, Any?>() }

    var notifyConfigurationChanges = true
    val entries: Set<Map.Entry<String, Any?>> = delegate.entries

    /**
     * Returns the value corresponding to the given [key] or `null` if such a key is not present in the map.
     *
     * @param key some key
     *
     * @return value corresponding to the given `key` or `null`
     */
    operator fun <T> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return delegate[key] as T?
    }

    /**
     * Associates the specified [value] with the specified [key] in the map.
     *
     * @param key some key
     * @param value some value
     *
     * @return previous value associated with the `key` or `null` if the `key` was not present in the map.
     */
    operator fun <T> set(key: String, value: T?): T? {
        val result = delegate.put(key, value)
        if (result != value) {
            notifyObservers()
        }
        @Suppress("UNCHECKED_CAST")
        return result as T?
    }

    /**
     * Returns the value corresponding to the given [key] or [defaultValue] if such a `key` is not present in the map.
     *
     * @param key some key
     * @param defaultValue default value
     *
     * @return value corresponding to the given `key` or `defaultValue`
     */
    fun <T> getOrDefault(key: String, defaultValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return delegate[key] as T? ?: defaultValue
    }

    /**
     * Returns the value corresponding to the given [key] or value from [defaultProvider] if such a `key` is not present in the map.
     *
     * @param key some key
     * @param defaultProvider default value provider
     *
     * @return value corresponding to the given `key` or `defaultValue`
     */
    fun <T> getOrDefault(key: String, defaultProvider: () -> T): T {
        val data: T? = this[key]
        return if (data != null) {
            data
        } else {
            notifyConfigurationChanges = false
            val newData = defaultProvider()
            this[key] = newData
            notifyConfigurationChanges = true
            newData
        }
    }

    /**
     * Updates this configuration with key/value pairs from the specified map.
     *
     * @param config some map
     */
    fun load(config: Map<String, *>) {
        if (config.isNotEmpty()) {
            delegate.putAll(config)
            notifyObservers()
        }
    }

    /**
     * Updates this configuration with key/value pairs.
     *
     * @param config some map
     */
    fun load(config: Array<Pair<String, *>>) {
        load(config.toMap())
    }

    /**
     * Updates this configuration with key/value pairs.
     *
     * @param config some map
     */
    fun load(config: Iterable<Pair<String, *>>) {
        load(config.toMap())
    }

    /**
     * Removes all elements from this configuration.
     */
    fun clear() {
        if (delegate.isNotEmpty()) {
            delegate.clear()
            notifyObservers()
        }
    }

    /**
     * Removes the specified [key] and its corresponding value from this configuration.
     *
     * @param key some key
     */
    fun remove(key: String) {
        if (delegate.remove(key) != null) {
            notifyObservers()
        }
    }

    /**
     * Notify observers that configuration has been changed even if it's not true.
     */
    fun notifyConfigurationChanged() {
        notifyConfigurationChanges = true
        notifyObservers()
    }

    private fun notifyObservers() {
        if (notifyConfigurationChanges) {
            observable.notifyObservers { it.onConfigurationChanged(this) }
        }
    }

    /**
     * Callback that will be called once [Configuration] changed.
     */
    @FunctionalInterface
    interface OnConfigurationChangedListener {

        /**
         * Called when an associated [configuration] changed.
         *
         * @param configuration instance of [Configuration]
         */
        fun onConfigurationChanged(configuration: Configuration)
    }
}