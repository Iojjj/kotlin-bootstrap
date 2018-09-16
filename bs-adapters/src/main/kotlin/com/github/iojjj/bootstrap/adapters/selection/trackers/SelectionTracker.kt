package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.Selection
import com.github.iojjj.bootstrap.core.Predicate
import com.github.iojjj.bootstrap.utils.Observable

/**
 * Tracker used to store information about selected items.
 *
 * @property selection Object that holds selected items.
 *
 * @param T type of items
 */
interface SelectionTracker<T> : Observable<SelectionTracker.SelectionObserver<T>> {

    /**
     * Factory that creates new [SelectionTracker]'s builders.
     */
    companion object Factory {

        /**
         * Marker sent to elements of adapter when selection changes.
         */
        const val SELECTION_CHANGED_MARKER = "SELECTION_CHANGED"

        /**
         * Create a new [SelectionTracker] builder.
         *
         * @return a new builder.
         */
        fun <T> newBuilder(): StageType<T> = SelectionTrackerImpl.Builder()
    }

    val selection: Selection<T>

    /**
     * Check if selection contains item.
     *
     * @param item some item
     */
    fun isSelected(item: T): Boolean

    /**
     * Select a single item.
     *
     * @param item some item
     *
     * @return `true` if item has been selected, `false` otherwise
     */
    fun select(item: T): Boolean

    /**
     * Select multiple items.
     *
     * @param items some items
     *
     * @return `true` if at least one item has been selected, `false` otherwise
     */
    fun select(items: Iterable<T>): Boolean

    /**
     * Select multiple items.
     *
     * @param items some items
     *
     * @return `true` if at least one item has been selected, `false` otherwise
     */
    fun select(items: Array<T>): Boolean

    /**
     * Deselect a single item.
     *
     * @param item some item
     *
     * @return `true` if item has been deselected, `false` otherwise
     */
    fun deselect(item: T): Boolean

    /**
     * Deselect multiple items.
     *
     * @param items some items
     *
     * @return `true` if at least one item has been deselected, `false` otherwise
     */
    fun deselect(items: Iterable<T>): Boolean

    /**
     * Deselect multiple items.
     *
     * @param items some items
     *
     * @return `true` if at least one item has been deselected, `false` otherwise
     */
    fun deselect(items: Array<T>): Boolean

    /**
     * Toggle selection of a single item.
     *
     * @param item some item
     *
     * @return `true` if item has been selected, `false` - if deselected
     */
    fun toggle(item: T): Boolean

    /**
     * Clear selection.
     */
    fun clear()

    /**
     * Check if selection is actual and remove all items that are not in adapter anymore.
     */
    fun checkSelection()

    /**
     * Observer that notified when selection state changes.
     *
     * @param T type of items
     */
    interface SelectionObserver<T> {

        /**
         * Called when selection started, i.e. a first item (or items) has been added to selection.
         *
         * This method called **before** [onSelectionChanged].
         *
         * @param selection selection triggered this observer
         */
        fun onSelectionStarted(selection: Selection<T>)

        /**
         * Called when selection changed, i.e. an item (or items) has been added to or removed from selection.
         *
         * @param selection selection triggered this observer
         */
        fun onSelectionChanged(selection: Selection<T>)

        /**
         * Called when selection stopped, i.e. a last item (or items) has been removed from selection.
         *
         * This method called **after** [onSelectionChanged].
         *
         * @param selection selection triggered this observer
         */
        fun onSelectionStopped(selection: Selection<T>)
    }

    /**
     * Empty implementation of [SelectionObserver].
     *
     * @param T type of items
     */
    abstract class SelectionAdapter<T> : SelectionObserver<T> {

        override fun onSelectionStarted(selection: Selection<T>) {
            /* no-op */
        }

        override fun onSelectionChanged(selection: Selection<T>) {
            /* no-op */
        }

        override fun onSelectionStopped(selection: Selection<T>) {
            /* no-op */
        }
    }

    /**
     * Predicate used to check if item can be selected.
     */
    @FunctionalInterface
    interface SelectionPredicate<T> {

        /**
         * Check if item can be selected.
         *
         * @param item item that probably can be selected
         *
         * @return `true` if item can be selected, `false` otherwise
         */
        fun test(item: T): Boolean
    }

    /**
     * Builder stage that allows to choose selection type.
     *
     * @param T type of items
     */
    interface StageType<T> {

        /**
         * Initialize selection tracker that allows to select only one item.
         *
         * @return
         */
        fun withSingleSelection(): StageObservers<T>

        /**
         * Initialize selection tracker that allows to select multiple items.
         *
         * @return same builder for chaining calls
         */
        fun withMultipleSelection(): StageObservers<T>
    }

    /**
     * Builder stage that allows to define optional settings.
     */
    interface StageObservers<T> {

        /**
         * Set predicate to filter items that can be selected.
         *
         * @param selectionPredicate instance of predicate
         *
         * @return same builder for chaining calls
         */
        fun withSelectionPredicate(selectionPredicate: SelectionPredicate<T>): StageObservers<T> = withSelectionPredicate(selectionPredicate::test)

        /**
         * Set predicate to filter items that can be selected.
         *
         * @param selectionPredicate instance of predicate
         *
         * @return same builder for chaining calls
         */
        fun withSelectionPredicate(selectionPredicate: Predicate<T>): StageObservers<T>

        /**
         * Add an observer to receive selection changes callbacks.
         *
         * @param selectionObserver instance of [SelectionObserver]
         *
         * @return same builder for chaining calls
         */
        fun addObserver(selectionObserver: SelectionObserver<T>): StageObservers<T>

        /**
         * Remove a previously added observer.
         *
         * @param selectionObserver instance of [SelectionObserver]
         *
         * @return same builder for chaining calls
         */
        fun removeObserver(selectionObserver: SelectionObserver<T>): StageObservers<T>

        /**
         * Clear all added observers.
         */
        fun clearObservers(): StageObservers<T>
    }
}