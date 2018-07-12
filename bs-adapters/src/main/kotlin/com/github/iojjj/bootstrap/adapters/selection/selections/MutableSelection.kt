package com.github.iojjj.bootstrap.adapters.selection.selections

/**
 * Mutable implementation of [Selection] that allows to modify itself.
 *
 * @param T type of items
 */
interface MutableSelection<T> : Selection<T> {

    /**
     * Add an item to selection.
     *
     * @param item some item
     *
     * @return `true` if selection was modified, `false` otherwise
     */
    fun add(item: T): Boolean

    /**
     * Remove an item from selection.
     *
     * @param item some item
     *
     * @return `true` if selection was modified, `false` otherwise
     */
    fun remove(item: T): Boolean

    /**
     * Clear selection.
     */
    fun clear()
}