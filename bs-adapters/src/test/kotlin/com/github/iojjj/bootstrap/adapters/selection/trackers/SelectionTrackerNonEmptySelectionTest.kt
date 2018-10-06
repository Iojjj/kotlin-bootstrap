package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.MutableSelection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

abstract class SelectionTrackerNonEmptySelectionTest(deferredSelection: () -> MutableSelection<Int>) : SelectionTrackerTestCase(deferredSelection) {

    @Before
    override fun setUp() {
        super.setUp()
        tracker.select(1)
    }

    // GIVEN selection is empty
    // WHEN predicate does not allow
    // AND adapter contains item
    // AND select single
    // THEN selection should not be changed

    @Test
    fun select_whenPredicateDoesNotAllow_andSelectSingle_thenShouldNotChangeSelection() {
        setPredicateAllows(false)
        assertFalse(tracker.select(1))
        assertTrue(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }


    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND select same single
    // THEN selection should not be changed

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectSameSingle_thenShouldNotChangeSelection() {
        assertFalse(tracker.select(1))
        assertTrue(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND deselect different single
    // THEN selection should not be changed

    @Test
    fun deselect_whenPredicateAllows_andAdapterContainsItem_andDeselectDifferentSingle_thenShouldNotChangeSelection() {
        assertFalse(tracker.deselect(2))
        assertFalse(tracker.isSelected(2))
        assertTrue(tracker.isSelected(1))

        verifySelectionObserverCalledOneTime()
        verify(listUpdateCallback, never()).onChanged(2, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyListUpdateCallbackCalledOneTime()
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND deselect same single
    // THEN selection should be changed

    @Test
    fun deselect_whenPredicateAllows_andAdapterContainsItem_andDeselectSameSingle_thenShouldChangeSelection() {
        assertTrue(tracker.deselect(1))
        assertFalse(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer).onSelectionStopped(selection)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate does not allow
    // AND deselect single
    // THEN selection should not be changed

    @Test
    fun deselect_whenPredicateDoesNotAllow_andDeselectSingle_thenShouldNotChangeSelection() {
        setPredicateAllows(false)
        assertFalse(tracker.deselect(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND toggle same single
    // THEN selection should be changed

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleSameSingle_thenShouldChangeSelection() {
        tracker.toggle(1)
        assertFalse(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer).onSelectionStopped(selection)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is not empty
    // WHEN check is selected
    // THEN should return true or false

    @Test
    fun isSelected_whenIsSelected_thenShouldReturnTrueOrFalse() {
        assertTrue(tracker.isSelected(1))
        assertFalse(tracker.isSelected(2))
        assertFalse(tracker.isSelected(3))
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND clear selection
    // THEN selection should be changed

    @Test
    fun clear_whenPredicateAllows_andAdapterContainsItem_thenShouldChangeSelection() {
        tracker.clear()
        assertFalse(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer).onSelectionStopped(selection)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is not empty
    // WHEN predicate doesn't allow
    // AND adapter contains item
    // AND clear selection
    // THEN selection should be changed

    @Test
    fun clear_whenPredicateAllows_andAdapterContainsItem_thenShouldNotChangeSelection() {
        setPredicateAllows(false)
        tracker.clear()
        assertTrue(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter doesn't contain item
    // AND clear selection
    // THEN should throw exception

    @Test(expected = IllegalArgumentException::class)
    fun clear_whenPredicateAllows_andAdapterContainsItem_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.clear()
    }
}