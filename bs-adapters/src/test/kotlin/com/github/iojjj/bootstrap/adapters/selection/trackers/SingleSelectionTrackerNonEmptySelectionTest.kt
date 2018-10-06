package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.SingleSelection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*

class SingleSelectionTrackerNonEmptySelectionTest : SelectionTrackerNonEmptySelectionTest({ SingleSelection() }) {

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND select new single
    // THEN selection should be changed

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectNewSingle_adapterSibling_thenShouldChangeSelection() {
        assertTrue(tracker.select(2))
        assertTrue(tracker.isSelected(2))
        assertFalse(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(1, 2, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectNewSingle_adapterNotSibling_thenShouldChangeSelection() {
        assertTrue(tracker.select(3))
        assertTrue(tracker.isSelected(3))
        assertFalse(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND toggle different single
    // THEN selection should be changed

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleDifferentSingle_adapterSibling_thenShouldChangeSelection() {
        tracker.toggle(2)
        assertFalse(tracker.isSelected(1))
        assertTrue(tracker.isSelected(2))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(1, 2, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleDifferentSingle_adapterNotSibling_thenShouldNotify_listUpdateCallback() {
        tracker.toggle(3)
        assertFalse(tracker.isSelected(1))
        assertTrue(tracker.isSelected(3))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }
}