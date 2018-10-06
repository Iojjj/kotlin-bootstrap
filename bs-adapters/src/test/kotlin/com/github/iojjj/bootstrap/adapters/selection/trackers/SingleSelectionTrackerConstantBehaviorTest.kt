package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.SingleSelection
import org.junit.Test

class SingleSelectionTrackerConstantBehaviorTest : SelectionTrackerConstantBehaviorTestCase({ SingleSelection() }) {

    // GIVEN selection is empty
    // AND select multiple
    // THEN should throw UnsupportedOperationException

    @Test(expected = UnsupportedOperationException::class)
    fun select_whenSelectArray_shouldThrowException() {
        tracker.select(arrayOf(1, 2, 3))
    }

    @Test(expected = UnsupportedOperationException::class)
    fun select_whenSelectIterable_shouldThrowException() {
        tracker.select(listOf(1, 2, 3))
    }

    // GIVEN selection is empty
    // AND deselect multiple
    // THEN should throw UnsupportedOperationException

    @Test(expected = UnsupportedOperationException::class)
    fun deselect_whenDeselectArray_shouldThrowException() {
        tracker.deselect(arrayOf(1, 2, 3))
    }

    @Test(expected = UnsupportedOperationException::class)
    fun deselect_whenDeselectIterable_shouldThrowException() {
        tracker.deselect(listOf(1, 2, 3))
    }

    // GIVEN selection is empty
    // WHEN toggle multiple
    // THEN should throw UnsupportedOperationException

    @Test(expected = UnsupportedOperationException::class)
    fun toggle_whenToggleArray_shouldThrowException() {
        tracker.toggle(arrayOf(1, 2, 3))
    }

    @Test(expected = UnsupportedOperationException::class)
    fun toggle_whenToggleIterable_shouldThrowException() {
        tracker.toggle(listOf(1, 2, 3))
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND deselect multiple
    // THEN should throw UnsupportedOperationException

    @Test(expected = UnsupportedOperationException::class)
    fun deselect_whenDeselectMultipleArray_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.deselect(arrayOf(1, 2, 3))
    }

    @Test(expected = UnsupportedOperationException::class)
    fun deselect_whenDeselectMultipleIterable_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.deselect(listOf(1, 2, 3))
    }
}