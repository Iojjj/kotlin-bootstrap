package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.SingleSelection
import org.junit.Assert.assertFalse
import org.junit.Test

class SingleSelectionTrackerEmptySelectionTest : SelectionTrackerEmptySelectionTestCase({ SingleSelection() }) {

    // GIVEN selection is empty
    // WHEN predicate does not allow
    // AND adapter contains item
    // AND select single
    // THEN selection should not be changed

    @Test
    fun select_whenPredicateDoesNotAllow_andSelectSingle_thenShouldNotChangeSelection() {
        setPredicateAllows(false)
        assertFalse(tracker.select(1))
        assertFalse(tracker.isSelected(1))

        verifySelectionObserverNotCalled()
        verifyListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate does not allow
    // AND deselect single
    // THEN selection should not be changed

    @Test
    fun deselect_whenPredicateDoesNotAllow_andDeselectSingle_thenShouldNotChangeSelection() {
        setPredicateAllows(false)
        assertFalse(tracker.deselect(1))

        verifySelectionObserverNotCalled()
        verifyListUpdateCallbackNotCalled()
    }
}