package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.MultipleSelection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class MultipleSelectionTrackerNonEmptySelectionTest : SelectionTrackerNonEmptySelectionTest({ MultipleSelection() }) {

    @Before
    override fun setUp() {
        super.setUp()
        tracker.select(1)
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND select multiple empty
    // THEN selection should not be changed

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectMultipleEmptyArray_thenShouldNotChangeSelection() {
        assertFalse(tracker.select(emptyArray()))

        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectMultipleEmptyIterable_thenShouldNotChangeSelection() {
        assertFalse(tracker.select(emptyList()))

        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND select multiple
    // THEN selection should be changed

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectMultipleArray_adapterSiblings_thenShouldChangeSelection() {
        assertTrue(tracker.select(arrayOf(1, 2, 3)))
        assertTrue(tracker.isSelected(1))
        assertTrue(tracker.isSelected(2))
        assertTrue(tracker.isSelected(3))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback).onChanged(2, 2, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyListUpdateCallbackCalledOneTime()
    }

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectMultipleIterable_adapterSiblings_thenShouldChangeSelection() {
        assertTrue(tracker.select(listOf(1, 2, 3)))
        assertTrue(tracker.isSelected(1))
        assertTrue(tracker.isSelected(2))
        assertTrue(tracker.isSelected(3))

        verify(listUpdateCallback).onChanged(2, 2, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyListUpdateCallbackCalledOneTime()
    }

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectMultipleArray_adapterNotSiblings_thenShouldChangeSelection() {
        assertTrue(tracker.select(arrayOf(1, 3, 5)))
        assertTrue(tracker.isSelected(1))
        assertTrue(tracker.isSelected(3))
        assertTrue(tracker.isSelected(5))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(5, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectMultipleIterable_adapterNotSiblings_thenShouldChangeSelection() {
        assertTrue(tracker.select(listOf(1, 3, 5)))
        assertTrue(tracker.isSelected(1))
        assertTrue(tracker.isSelected(3))
        assertTrue(tracker.isSelected(5))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(5, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND select multiple empty
    // THEN should not change selection

    @Test
    fun select_whenPredicateAllows_andAdapterDoesNotContainItem_andSelectMultipleArrayEmpty_thenShouldNotChangeSelection() {
        setAdapterContainsItem(false)
        assertFalse(tracker.select(emptyArray()))
        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    @Test
    fun select_whenPredicateAllows_andAdapterDoesNotContainItem_andSelectMultipleIterableEmpty_thenShouldNotChangeSelection() {
        setAdapterContainsItem(false)
        assertFalse(tracker.select(emptyList()))
        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    // GIVEN selection is not empty
    // WHEN predicate allows
    // AND adapter contains item
    // AND select new single
    // THEN selection should be changed

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectNewSingle_adapterSibling_thenShouldChangeSelection() {
        assertTrue(tracker.select(2))
        assertTrue(tracker.isSelected(2))
        assertTrue(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(2, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun select_whenPredicateAllows_andAdapterContainsItem_andSelectNewSingle_adapterNotSibling_thenShouldChangeSelection() {
        assertTrue(tracker.select(3))
        assertTrue(tracker.isSelected(3))
        assertTrue(tracker.isSelected(1))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter contains items
    // AND deselect multiple
    // THEN selection should not be changed

    @Test
    fun deselect_whenPredicateAllows_andAdapterContainsItem_andDeselectMultipleArray_thenShouldChangeSelection() {
        assertTrue(tracker.deselect(arrayOf(1, 2, 3)))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer).onSelectionStopped(selection)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback, never()).onChanged(2, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback, never()).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun deselect_whenPredicateAllows_andAdapterContainsItem_andDeselectMultipleIterable_thenShouldChangeSelection() {
        assertTrue(tracker.deselect(listOf(1, 2, 3)))

        verify(observer).onSelectionStarted(selection)
        verify(observer, times(2)).onSelectionChanged(selection)
        verify(observer).onSelectionStopped(selection)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback, never()).onChanged(2, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback, never()).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate does not allow
    // AND deselect single
    // THEN selection should not be changed

    @Test
    fun deselect_whenPredicateDoesNotAllow_andDeselectSingle_thenShouldNotDeselect() {
        setPredicateAllows(false)
        assertFalse(tracker.deselect(1))
        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    // GIVEN selection is empty
    // WHEN predicate does not allow
    // AND deselect multiple
    // THEN selection should not be changed

    @Test
    fun deselect_whenPredicateDoesNotAllow_andDeselectMultipleArray_thenShouldNotDeselect() {
        setPredicateAllows(false)
        assertFalse(tracker.deselect(arrayOf(1, 2, 3)))
        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    @Test
    fun deselect_whenPredicateDoesNotAllow_andDeselectMultipleIterable_thenShouldNotDeselect() {
        setPredicateAllows(false)
        assertFalse(tracker.deselect(listOf(1, 2, 3)))
        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }


    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND deselect multiple empty
    // THEN should not change selection

    @Test
    fun deselect_whenPredicateAllows_andAdapterDoesNotContainItem_andDeselectMultipleArrayEmpty_thenShouldNotChangeSelection() {
        setAdapterContainsItem(false)
        assertFalse(tracker.deselect(emptyArray()))
        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    @Test
    fun deselect_whenPredicateAllows_andAdapterDoesNotContainItem_andDeselectMultipleIterableEmpty_thenShouldNotChangeSelection() {
        setAdapterContainsItem(false)
        assertFalse(tracker.deselect(emptyList()))
        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter contains items
    // AND toggle multiple
    // THEN should change selection

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleArray_adapterSiblings_thenShouldChangeSelection() {
        tracker.toggle(arrayOf(1, 2, 3))
        assertFalse(tracker.isSelected(1))
        assertTrue(tracker.isSelected(2))
        assertTrue(tracker.isSelected(3))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback).onChanged(1, 3, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleIterable_adapterSiblings_thenShouldChangeSelection() {
        tracker.toggle(listOf(1, 2, 3))
        assertFalse(tracker.isSelected(1))
        assertTrue(tracker.isSelected(2))
        assertTrue(tracker.isSelected(3))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback).onChanged(1, 3, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleArray_adapterNotSiblings_thenShouldChangeSelection() {
        tracker.toggle(arrayOf(1, 3, 5))
        assertFalse(tracker.isSelected(1))
        assertTrue(tracker.isSelected(3))
        assertTrue(tracker.isSelected(5))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(5, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleIterable_adapterNotSiblings_thenShouldChangeSelection() {
        tracker.toggle(listOf(1, 3, 5))
        assertFalse(tracker.isSelected(1))
        assertTrue(tracker.isSelected(3))
        assertTrue(tracker.isSelected(5))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback, times(2)).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(5, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
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
        assertTrue(tracker.isSelected(1))
        assertTrue(tracker.isSelected(2))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(2, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleDifferentSingle_adapterNotSibling_thenShouldNotify_listUpdateCallback() {
        tracker.toggle(3)
        assertTrue(tracker.isSelected(1))
        assertTrue(tracker.isSelected(3))

        verifySelectionObserverCalledOneTime(2)

        verify(listUpdateCallback).onChanged(1, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verify(listUpdateCallback).onChanged(3, 1, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter contains items
    // AND toggle multiple empty
    // THEN should not change selection

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleArrayEmpty_thenShouldNotChangeSelection() {
        tracker.toggle(emptyArray())
        assertTrue(tracker.isSelected(1))
        assertFalse(tracker.isSelected(2))
        assertFalse(tracker.isSelected(3))

        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }

    @Test
    fun toggle_whenPredicateAllows_andAdapterContainsItem_andToggleIterableEmpty_thenShouldNotChangeSelection() {
        tracker.toggle(emptyList())
        assertTrue(tracker.isSelected(1))
        assertFalse(tracker.isSelected(2))
        assertFalse(tracker.isSelected(3))

        verifySelectionObserverCalledOneTime()
        verifyListUpdateCallbackCalledOneTime()
    }
}