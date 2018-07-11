@file:Suppress("NOTHING_TO_INLINE")

package com.github.iojjj.bootstrap.utils

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Simple interface of `Observable` pattern.
 *
 * @param T type of observer
 */
class InvokableObservable<T> private constructor() : Observable<T> {

    companion object {

        /**
         * Create a new instance of [InvokableObservable].
         *
         * @return a new instance of observable
         */
        @JvmStatic
        fun <T> newInstance(): InvokableObservable<T> = InvokableObservable()
    }

    private val observers: MutableList<T> by lazy { CopyOnWriteArrayList<T>() }

    override fun addObserver(observer: T): Boolean = observers.add(observer)

    override fun removeObserver(observer: T): Boolean = observers.remove(observer)

    override fun clearObservers() = observers.clear()

    /**
     * Notify listeners about some event risen.
     *
     * @param consumer consumer that rises a proper event of observer
     */
    fun notifyObservers(consumer: Consumer<T>) = observers.forEach(consumer)
}

/**
 * Create a new instance of [InvokableObservable].
 *
 * @param observers array of observers that must be registered after object instantiation
 *
 * @return a new instance of observable
 */
inline fun <T> observableOf(vararg observers: T): InvokableObservable<T> {
    return InvokableObservable.newInstance<T>().apply {
        observers.forEach { addObserver(it) }
    }
}