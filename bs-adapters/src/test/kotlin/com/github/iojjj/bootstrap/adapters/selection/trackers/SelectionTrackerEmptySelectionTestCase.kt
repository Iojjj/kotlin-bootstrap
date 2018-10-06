package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.MutableSelection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

abstract class SelectionTrackerEmptySelectionTestCase(deferredSelection: () -> MutableSelection<Int>) : SelectionTrackerTestCase(deferredSelection) {

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND select single
    // THEN selection should be changed

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectSingle_thenShouldChangeSelection() {
        assertTrue(tracker.select(1))
        assertTrue(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND deselect single
    // THEN selection should not be changed

    @Test
    fun deselect_whenPredicateAllows_andAdapterContainsItem_andDeselectSingle_thenShouldNotChangeSelection() {
        assertFalse(tracker.deselect(1))
        verifySelectionObserverNotCalled()
        verifyListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND toggle single
    // THEN selection should be changed

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleSingle_thenShouldChangeSelection() {
        assertFalse(tracker.isSelected(1))
        tracker.toggle(1)
        assertTrue(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN check is selected
    // THEN should return false

    @Test
    fun isSelected_whenIsSelected_thenShouldReturnFalse() {
        assertFalse(tracker.isSelected(1))
        assertFalse(tracker.isSelected(2))
        assertFalse(tracker.isSelected(3))
    }

    // GIVEN selection is empty
    // WHEN clear selection
    // THEN selection should not be changed

    @Test
    fun clear_whenClearSelection_thenShouldNotChangeSelection() {
        tracker.clear()
        verifySelectionObserverNotCalled()
        verifyListUpdateCallbackNotCalled()
    }
}