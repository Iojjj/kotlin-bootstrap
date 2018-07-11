package com.github.iojjj.bootstrap.utils

/**
 * Simple interface of `Observable` pattern.
 *
 * @param T type of observer
 */
interface Observable<in T> {

    /**
     * Add a new observer.
     *
     * @param observer observer that should be added.
     *
     * @return `true` if observer was added, `false` otherwise.
     */
    fun addObserver(observer: T): Boolean

    /**
     * Remove a observer.
     *
     * @param observer observer that should be removed.
     *
     * @return `true` if observer was removed, `false` otherwise.
     */
    fun removeObserver(observer: T): Boolean

    /**
     * Remove all added observers.
     */
    fun clearObservers()
}