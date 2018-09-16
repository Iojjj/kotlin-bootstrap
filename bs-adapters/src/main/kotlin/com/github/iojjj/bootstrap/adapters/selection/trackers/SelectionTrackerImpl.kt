package com.github.iojjj.bootstrap.adapters.selection.trackers

import android.support.v7.util.BatchingListUpdateCallback
import com.github.iojjj.bootstrap.adapters.selection.selections.MultipleSelection
import com.github.iojjj.bootstrap.adapters.selection.selections.MutableSelection
import com.github.iojjj.bootstrap.adapters.selection.selections.SingleSelection
import com.github.iojjj.bootstrap.core.Predicate
import com.github.iojjj.bootstrap.core.Predicates
import com.github.iojjj.bootstrap.utils.InvokableObservable
import com.github.iojjj.bootstrap.utils.Observable
import com.github.iojjj.bootstrap.utils.observableOf

/**
 * Implementation of [SelectionTracker].
 *
 * @param T type of items
 */
internal class SelectionTrackerImpl<T>(
        override val selection: MutableSelection<T>,
        private val predicate: Predicate<T>,
        private val positionMapper: (T) -> Int,
        private val callback: BatchingListUpdateCallback,
        private val observable: InvokableObservable<SelectionTracker.SelectionObserver<T>> = observableOf())
    :
        SelectionTracker<T>,
        Observable<SelectionTracker.SelectionObserver<T>> by observable {

    override fun isSelected(item: T): Boolean = selection.contains(item)

    override fun select(item: T): Boolean = changeSelection(listOf(item), true)

    override fun select(items: Iterable<T>): Boolean = changeSelection(items, true)

    override fun select(items: Array<T>): Boolean = changeSelection(items.asIterable(), true)

    override fun deselect(item: T): Boolean = changeSelection(listOf(item), false)

    override fun deselect(items: Iterable<T>): Boolean = changeSelection(items, false)

    override fun deselect(items: Array<T>): Boolean = changeSelection(items.asIterable(), false)

    override fun toggle(item: T): Boolean {
        return if (selection.contains(item)) {
            deselect(item)
            false
        } else {
            select(item)
            true
        }
    }

    override fun clear() {
        val snapshot = selection.snapshot()
        changeSelection(snapshot, false)
    }

    override fun checkSelection() {
        val snapshot = selection.snapshot()
        val toRemove = snapshot.filter { positionMapper(it) < 0 }
        changeSelection(toRemove, false)
    }

    private fun changeSelection(items: Iterable<T>, selected: Boolean): Boolean {
        if (!items.iterator().hasNext()) {
            // empty collection
            return false
        }
        val wasEmpty = selection.isEmpty()
        val singleItem = if (!wasEmpty && selection is SingleSelection) {
            selection.iterator().next()
        } else {
            null
        }
        val operation: (T) -> Boolean = if (selected) selection::add else selection::remove
        val changedItems = items
                .filter(predicate)
                .filter(operation)
                .toMutableList()
        return if (changedItems.isNotEmpty()) {
            val becameEmpty = selection.isEmpty()
            if (wasEmpty && !becameEmpty) {
                observable.notifyObservers { it.onSelectionStarted(selection) }
            }
            if (singleItem != null) {
                changedItems.add(singleItem)
            }
            notifySelectionChanged(changedItems)
            observable.notifyObservers { it.onSelectionChanged(selection) }
            if (!wasEmpty && becameEmpty) {
                observable.notifyObservers { it.onSelectionStopped(selection) }
            }
            true
        } else {
            false
        }
    }

    private fun notifySelectionChanged(items: Collection<T>) {
        items
                .map(positionMapper)
                .filter { it > -1 }
                .forEach { callback.onChanged(it, 1, SelectionTracker.SELECTION_CHANGED_MARKER) }
        callback.dispatchLastEvent()
    }

    internal class Builder<T> :
            SelectionTracker.StageType<T>,
            SelectionTracker.StageObservers<T> {

        internal lateinit var selection: MutableSelection<T>
        internal var selectionPredicate: Predicate<T> = Predicates.ALWAYS_TRUE
        internal lateinit var positionMapper: (T) -> Int
        internal lateinit var callback: BatchingListUpdateCallback
        internal val observable by lazy { observableOf<SelectionTracker.SelectionObserver<T>>() }

        override fun withSingleSelection(): SelectionTracker.StageObservers<T> {
            selection = SingleSelection()
            return this
        }

        override fun withMultipleSelection(): SelectionTracker.StageObservers<T> {
            selection = MultipleSelection()
            return this
        }

        override fun withSelectionPredicate(selectionPredicate: Predicate<T>): SelectionTracker.StageObservers<T> {
            this.selectionPredicate = selectionPredicate
            return this
        }

        override fun addObserver(selectionObserver: SelectionTracker.SelectionObserver<T>): SelectionTracker.StageObservers<T> {
            observable.addObserver(selectionObserver)
            return this
        }

        override fun removeObserver(selectionObserver: SelectionTracker.SelectionObserver<T>): SelectionTracker.StageObservers<T> {
            observable.removeObserver(selectionObserver)
            return this
        }

        override fun clearObservers(): SelectionTracker.StageObservers<T> {
            observable.clearObservers()
            return this
        }
    }
}

/**
 * Set position mapper that will map items to their adapter positions.
 *
 * @param positionMapper instance of mapper
 *
 * @return same builder for chaining calls
 */
internal fun <T> SelectionTracker.StageObservers<T>.withPositionMapper(positionMapper: (T) -> Int): SelectionTracker.StageObservers<T> {
    val that = this as SelectionTrackerImpl.Builder<T>
    that.positionMapper = positionMapper
    return this
}

/**
 * Set list update callback that will be called when selection changes.
 *
 * @param callback instance of `BatchingListUpdateCallback`
 *
 * @return same builder for chaining calls
 */
internal fun <T> SelectionTracker.StageObservers<T>.withListUpdateCallback(callback: BatchingListUpdateCallback):
        SelectionTracker.StageObservers<T> {
    val that = this as SelectionTrackerImpl.Builder<T>
    that.callback = callback
    return this
}

/**
 * Create a new instance of [SelectionTracker].
 *
 * @return a new instance of `SelectionTracker`
 */
internal fun <T> SelectionTracker.StageObservers<T>.build(): SelectionTracker<T> {
    val that = this as SelectionTrackerImpl.Builder<T>
    return SelectionTrackerImpl(that.selection, that.selectionPredicate, that.positionMapper, that.callback, that.observable)
}