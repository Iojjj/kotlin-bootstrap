package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.MutableSelection
import org.junit.Test

abstract class SelectionTrackerConstantBehaviorTestCase(deferredSelection: () -> MutableSelection<Int>) : SelectionTrackerTestCase(deferredSelection) {

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND select single
    // THEN should throw IllegalArgumentException

    @Test(expected = IllegalArgumentException::class)
    fun select_whenPredicateAllows_andAdapterDoesNotContainItem_andSelectSingle_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.select(1)
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND deselect single
    // THEN should throw IllegalArgumentException

    @Test(expected = IllegalArgumentException::class)
    fun deselect_whenPredicateAllows_andAdapterDoesNotContainItem_andDeselectSingle_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.deselect(1)
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND toggle single
    // THEN should throw IllegalArgumentException

    @Test(expected = IllegalArgumentException::class)
    fun deselect_whenPredicateAllows_andAdapterDoesNotContainItem_andToggleSingle_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.toggle(1)
    }
}