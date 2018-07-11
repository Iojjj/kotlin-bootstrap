package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.MultipleSelection
import com.github.iojjj.bootstrap.adapters.selection.selections.Selection

/**
 * Selection tracker that do nothing.
 */
internal object StubSelectionTracker : SelectionTracker<Any?> {

    override val selection: Selection<Any?> = MultipleSelection()

    override fun isSelected(item: Any?): Boolean = false

    override fun select(item: Any?): Boolean = false

    override fun select(items: Iterable<Any?>): Boolean = false

    override fun select(items: Array<Any?>): Boolean = false

    override fun deselect(item: Any?): Boolean = false

    override fun deselect(items: Iterable<Any?>): Boolean = false

    override fun deselect(items: Array<Any?>): Boolean = false

    override fun toggle(item: Any?): Boolean = false

    override fun clear() {
        /* no-op */
    }

    override fun checkSelection() {
        /* no-op */
    }

    override fun addObserver(observer: SelectionTracker.SelectionObserver<Any?>): Boolean = false

    override fun removeObserver(observer: SelectionTracker.SelectionObserver<Any?>): Boolean = false

    override fun clearObservers() {
        /* no-op */
    }

    internal fun <T> cast(): SelectionTracker<T> {
        @Suppress("UNCHECKED_CAST")
        return this as SelectionTracker<T>
    }
}