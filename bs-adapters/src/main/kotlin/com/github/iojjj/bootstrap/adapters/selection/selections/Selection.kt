package com.github.iojjj.bootstrap.adapters.selection.selections

/**
 * Selection that contains selected items.
 *
 * @param T type of items
 */
interface Selection<T> : Iterable<T> {

    /**
     * Number of selected items.
     */
    val size: Int

    /**
     * Check if there are at least one item in selection.
     *
     * @return `true` if selection is empty, `false` otherwise
     */
    fun isEmpty(): Boolean

    /**
     * Check if selection contains an item.
     *
     * @param item some item
     *
     * @return `true` if selection contains item, `false` otherwise
     */
    fun contains(item: T): Boolean

    /**
     * Create a mutable snapshot of `this` selection. Created snapshot will not receive any selection updates.
     *
     * @return a mutable snapshot of `this` selection.
     */
    fun snapshot(): MutableSelection<T>

}