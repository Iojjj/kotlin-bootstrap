package com.github.iojjj.bootstrap.adapters.selection.trackers

import android.support.v7.util.BatchingListUpdateCallback
import com.github.iojjj.bootstrap.adapters.selection.selections.MultipleSelection
import com.github.iojjj.bootstrap.adapters.selection.selections.MutableSelection
import com.github.iojjj.bootstrap.adapters.selection.selections.SingleSelection
import com.github.iojjj.bootstrap.utils.*

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
        snapshot.forEach {
            val position = positionMapper(it)
            if (position < 0) {
                selection.remove(it)
            }
        }
    }

    private fun changeSelection(items: Iterable<T>, selected: Boolean): Boolean {
        if (!items.iterator().hasNext()) {
            // empty collection
            return false
        }
        val wasEmpty = selection.isEmpty()
        val operation: (T) -> Boolean = if (selected) selection::add else selection::remove
        val changedItems = items
                .filter(predicate)
                .filter(operation)
        return if (changedItems.isNotEmpty()) {
            val becameEmpty = selection.isEmpty()
            if (wasEmpty && !becameEmpty) {
                observable.notifyObservers { it.onSelectionStarted(selection) }
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
            SelectionTracker.BuilderStageType<T>,
            SelectionTracker.BuilderStageSelectionPredicate<T>,
            SelectionTracker.BuilderStageFinal<T> {

        internal lateinit var selection: MutableSelection<T>
        internal var selectionPredicate: Predicate<T> = Predicates.ALWAYS_TRUE
        internal lateinit var positionMapper: (T) -> Int
        internal lateinit var callback: BatchingListUpdateCallback
        internal val observable by lazy { observableOf<SelectionTracker.SelectionObserver<T>>() }

        override fun withSingleSelection(): SelectionTracker.BuilderStageSelectionPredicate<T> {
            selection = SingleSelection()
            return this
        }

        override fun withMultipleSelection(): SelectionTracker.BuilderStageSelectionPredicate<T> {
            selection = MultipleSelection()
            return this
        }

        override fun withSelectionPredicate(selectionPredicate: SelectionTracker.SelectionPredicate<T>): SelectionTracker.BuilderStageFinal<T> {
            this.selectionPredicate = selectionPredicate::test
            return this
        }

        override fun withSelectionPredicate(selectionPredicate: Predicate<T>): SelectionTracker.BuilderStageFinal<T> {
            this.selectionPredicate = selectionPredicate
            return this
        }

        override fun addObserver(selectionObserver: SelectionTracker.SelectionObserver<T>): SelectionTracker.BuilderStageFinal<T> {
            observable.addObserver(selectionObserver)
            return this
        }

        override fun removeObserver(selectionObserver: SelectionTracker.SelectionObserver<T>): SelectionTracker.BuilderStageFinal<T> {
            observable.removeObserver(selectionObserver)
            return this
        }

        override fun clearObservers(): SelectionTracker.BuilderStageFinal<T> {
            observable.clearObservers()
            return this
        }
    }
}

internal fun <T> SelectionTracker.BuilderStageFinal<T>.withPositionMapper(positionMapper: (T) -> Int): SelectionTracker.BuilderStageFinal<T> {
    val that = this as SelectionTrackerImpl.Builder<T>
    that.positionMapper = positionMapper
    return this
}

internal fun <T> SelectionTracker.BuilderStageFinal<T>.withListUpdateCallback(callback: BatchingListUpdateCallback):
        SelectionTracker.BuilderStageFinal<T> {
    val that = this as SelectionTrackerImpl.Builder<T>
    that.callback = callback
    return this
}

internal fun <T> SelectionTracker.BuilderStageFinal<T>.build(): SelectionTracker<T> {
    val that = this as SelectionTrackerImpl.Builder<T>
    return SelectionTrackerImpl(that.selection, that.selectionPredicate, that.positionMapper, that.callback, that.observable)
}