package com.github.iojjj.bootstrap.adapters.selection.selections

/**
 * Implementation of [MutableSelection] that allows to select multiple items.
 *
 * @param T type of items
 */
internal class MultipleSelection<T> : MutableSelection<T> {

    private val items: MutableSet<T> by lazy { HashSet<T>() }

    override val size: Int
        get() = items.size

    constructor()
    constructor(from: Iterable<T>) : this() {
        items.addAll(from)
    }

    override fun isEmpty(): Boolean = items.isEmpty()

    override fun contains(item: T): Boolean = items.contains(item)

    override fun snapshot(): MutableSelection<T> = MultipleSelection(this)

    override fun iterator(): Iterator<T> = items.iterator()

    override fun add(item: T): Boolean = items.add(item)

    override fun remove(item: T): Boolean = items.remove(item)

    override fun clear() = items.clear()
}