package com.github.iojjj.bootstrap.adapters.selection.selections

/**
 * Implementation of [MutableSelection] that allows to select only one item.
 *
 * @param T type of items
 */
internal class SingleSelection<T> : MutableSelection<T> {

    companion object {

        /**
         * Object used to mark an empty selection state.
         */
        private val EMPTY = Any()
    }

    constructor()
    constructor(from: Iterable<T>) : this() {
        val iterator = from.iterator()
        if (iterator.hasNext()) {
            add(iterator.next())
        }
    }

    private var item: Any? = EMPTY

    override val size: Int
        get() = if (item == EMPTY) 0 else 1

    override fun isEmpty(): Boolean = item == EMPTY

    override fun contains(item: T): Boolean = this.item == item

    override fun snapshot(): MutableSelection<T> = SingleSelection(this)

    override fun iterator(): Iterator<T> {
        return SelectionIterator(item)
    }

    override fun add(item: T): Boolean {
        return if (contains(item)) {
            false
        } else {
            this.item = item
            true
        }
    }

    override fun remove(item: T): Boolean {
        return if (contains(item)) {
            this.item = EMPTY
            true
        } else {
            false
        }
    }

    override fun clear() {
        item = EMPTY
    }

    private class SelectionIterator<T>(private var nextItem: Any?) : AbstractIterator<T>() {

        @Suppress("UNCHECKED_CAST")
        override fun computeNext() {
            if (nextItem != EMPTY) {
                setNext(nextItem as T)
                nextItem = EMPTY
            } else {
                done()
            }
        }
    }

}
