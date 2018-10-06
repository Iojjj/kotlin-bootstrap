package com.github.iojjj.bootstrap.adapters.selection.trackers

import android.support.v7.util.BatchingListUpdateCallback
import com.github.iojjj.bootstrap.adapters.selection.selections.MultipleSelection
import com.github.iojjj.bootstrap.adapters.selection.selections.MutableSelection
import com.github.iojjj.bootstrap.adapters.selection.selections.SingleSelection
import com.github.iojjj.bootstrap.core.Predicate
import com.github.iojjj.bootstrap.core.Predicates
import com.github.iojjj.bootstrap.core.Predicates.ALWAYS_FALSE
import com.github.iojjj.bootstrap.core.Predicates.ALWAYS_TRUE
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

    private val isSingleSelection = selection is SingleSelection

    override fun isSelected(item: T): Boolean = selection.contains(item)

    override fun select(item: T): Boolean = changeSelection(listOf(item), ALWAYS_TRUE)

    override fun select(items: Iterable<T>): Boolean {
        checkUnsupportedOperation()
        return changeSelection(items, ALWAYS_TRUE)
    }

    override fun select(items: Array<T>): Boolean {
        checkUnsupportedOperation()
        return changeSelection(items.asIterable(), ALWAYS_TRUE)
    }

    override fun deselect(item: T): Boolean = changeSelection(listOf(item), ALWAYS_FALSE)

    override fun deselect(items: Iterable<T>): Boolean {
        checkUnsupportedOperation()
        return changeSelection(items, ALWAYS_FALSE)
    }

    override fun deselect(items: Array<T>): Boolean {
        checkUnsupportedOperation()
        return changeSelection(items.asIterable(), ALWAYS_FALSE)
    }

    override fun toggle(item: T) {
        changeSelection(listOf(item)) { !selection.contains(it) }
    }

    override fun toggle(items: Array<T>) {
        checkUnsupportedOperation()
        changeSelection(items.asIterable()) { !selection.contains(it) }
    }

    override fun toggle(items: Iterable<T>) {
        checkUnsupportedOperation()
        changeSelection(items) { !selection.contains(it) }
    }

    override fun clear() {
        val snapshot = selection.snapshot()
        changeSelection(snapshot, ALWAYS_FALSE)
    }

    override fun checkSelection() {
        val snapshot = selection.snapshot()
        val toRemove = snapshot.filter { positionMapper(it) < 0 }
        changeSelection(toRemove, ALWAYS_FALSE)
    }

    private fun changeSelection(items: Iterable<T>, isSelected: Predicate<T>): Boolean {
        if (!items.iterator().hasNext()) {
            // empty collection
            return false
        }
        checkItemsInAdapter(items)
        val wasEmpty = selection.isEmpty()
        val singleItem = if (!wasEmpty && isSingleSelection) {
            selection.iterator().next()
        } else {
            null
        }
        val operation: (T) -> Boolean = { if (isSelected(it)) selection.add(it) else selection.remove(it) }
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

    private fun checkItemsInAdapter(items: Iterable<T>) {
        val itemsOutsideOfAdapter = items
                .map(positionMapper)
                .filter { it == -1 }
                .count()
        if (itemsOutsideOfAdapter > 0) {
            if (itemsOutsideOfAdapter == 1) {
                throw IllegalArgumentException("Adapter doesn't contain 1 item.")
            } else {
                throw IllegalArgumentException("Adapter doesn't contain $items items.")
            }
        }
    }

    private fun notifySelectionChanged(items: Collection<T>) {
        items
                .map(positionMapper)
                .filter { it > -1 }
                .forEach { callback.onChanged(it, 1, SelectionTracker.SELECTION_CHANGED_MARKER) }
        callback.dispatchLastEvent()
    }

    private fun checkUnsupportedOperation() {
        if (isSingleSelection) {
            throw UnsupportedOperationException()
        }
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