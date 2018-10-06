package com.github.iojjj.bootstrap.adapters.selection.trackers

import com.github.iojjj.bootstrap.adapters.selection.selections.MultipleSelection
import org.junit.Assert
import org.junit.Test

class MultipleSelectionTrackerConstantBehaviorTest : SelectionTrackerConstantBehaviorTestCase({ MultipleSelection() }) {

    // GIVEN selection is empty
    // WHEN predicate does not allow
    // AND adapter contains items
    // AND select multiple
    // THEN selection should not be changed

    @Test
    fun select_whenPredicateDoesNotAllow_andSelectMultipleArray_thenShouldNotChangeSelection() {
        setPredicateAllows(false)
        Assert.assertFalse(tracker.select(arrayOf(1, 2, 3)))
        Assert.assertFalse(tracker.isSelected(1))
        Assert.assertFalse(tracker.isSelected(2))
        Assert.assertFalse(tracker.isSelected(3))

        verifySelectionObserverNotCalled()
        verifyListUpdateCallbackNotCalled()
    }

    @Test
    fun select_whenPredicateDoesNotAllow_andSelectMultipleIterable_thenShouldNotChangeSelection() {
        setPredicateAllows(false)
        Assert.assertFalse(tracker.select(listOf(1, 2, 3)))
        Assert.assertFalse(tracker.isSelected(1))
        Assert.assertFalse(tracker.isSelected(2))
        Assert.assertFalse(tracker.isSelected(3))

        verifySelectionObserverNotCalled()
        verifyListUpdateCallbackNotCalled()
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND select multiple
    // THEN should throw IllegalArgumentException

    @Test(expected = IllegalArgumentException::class)
    fun select_whenPredicateAllows_andAdapterDoesNotContainItem_andSelectMultipleArray_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.select(arrayOf(1, 2, 3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun select_whenPredicateAllows_andAdapterDoesNotContainItem_andSelectMultipleIterable_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.select(listOf(1, 2, 3))
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND deselect multiple
    // THEN should throw IllegalArgumentException

    @Test(expected = IllegalArgumentException::class)
    fun deselect_whenPredicateAllows_andAdapterDoesNotContainItem_andDeselectMultipleArray_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.deselect(arrayOf(1, 2, 3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun deselect_whenPredicateAllows_andAdapterDoesNotContainItem_andDeselectMultipleIterable_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.deselect(listOf(1, 2, 3))
    }

    // GIVEN selection is empty
    // WHEN predicate allows
    // AND adapter does not contain item
    // AND deselect multiple
    // THEN should throw IllegalArgumentException

    @Test(expected = IllegalArgumentException::class)
    fun toggle_whenPredicateAllows_andAdapterDoesNotContainItem_andToggleMultipleArray_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.toggle(arrayOf(1, 2, 3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun toggle_whenPredicateAllows_andAdapterDoesNotContainItem_andToggleMultipleIterable_thenShouldThrowException() {
        setAdapterContainsItem(false)
        tracker.toggle(listOf(1, 2, 3))
    }
}