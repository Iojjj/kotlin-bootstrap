package com.github.iojjj.bootstrap.adapters.selection.trackers

import android.support.annotation.CallSuper
import android.support.v7.util.BatchingListUpdateCallback
import android.support.v7.util.ListUpdateCallback
import com.github.iojjj.bootstrap.adapters.selection.selections.MutableSelection
import com.github.iojjj.bootstrap.core.Predicate
import com.github.iojjj.bootstrap.utils.InvokableObservable
import org.junit.Before
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.*

/**
 * JUnit test case for [SelectionTracker] class.
 * @property deferredSelection Provider of [selection]
 * @property tracker Instance of [SelectionTrackerImpl]
 * @property selection Instance of [MutableSelection] instantiated from [deferredSelection] and used in [tracker]
 * @property predicate Instance of selection predicate used in [tracker]
 * @property positionMapper Instance of position mapper used in [tracker]
 * @property listUpdateCallback Instance of [ListUpdateCallback] used in [tracker]
 * @property observer Instance of [SelectionTracker.SelectionObserver] used in [tracker]
 * @property invokableObservable Instance of [InvokableObservable] used in [tracker]
 */
@Suppress("UNCHECKED_CAST")
abstract class SelectionTrackerTestCase internal constructor(private val deferredSelection: () -> MutableSelection<Int>) {

    internal lateinit var tracker: SelectionTrackerImpl<Int>
    internal lateinit var selection: MutableSelection<Int>
    internal lateinit var predicate: Predicate<Int>
    internal lateinit var positionMapper: (Int) -> Int
    internal lateinit var listUpdateCallback: ListUpdateCallback
    internal lateinit var observer: SelectionTracker.SelectionObserver<Int>

    private lateinit var invokableObservable: InvokableObservable<SelectionTracker.SelectionObserver<Int>>

    @CallSuper
    @Before
    open fun setUp() {
        selection = deferredSelection()
        predicate = mock(Function1::class.java) as Function1<Int, Boolean>
        positionMapper = mock(Function1::class.java) as Function1<Int, Int>
        listUpdateCallback = mock(ListUpdateCallback::class.java)
        invokableObservable = InvokableObservable.newInstance()
        observer = mock(SelectionTracker.SelectionObserver::class.java) as SelectionTracker.SelectionObserver<Int>
        val batchingListUpdateCallback = BatchingListUpdateCallback(listUpdateCallback)
        tracker = SelectionTrackerImpl(selection, predicate, positionMapper, batchingListUpdateCallback, invokableObservable)
        tracker.addObserver(observer)

        setPredicateAllows(true)
        setAdapterContainsItem(true)
    }

    /**
     * Set behavior of [predicate].
     * @param allows `true` to allow any item, `false` otherwise
     */
    protected fun setPredicateAllows(allows: Boolean) {
        // always decline
        `when`(predicate(anyInt())).thenReturn(allows)
    }

    /**
     * Set behavior of [positionMapper].
     * @param contains `true` to contain any item, `false` otherwise
     */
    protected fun setAdapterContainsItem(contains: Boolean) {
        // return the same position as a passed argument
        `when`(positionMapper(anyInt())).thenAnswer {
            if (contains) {
                it.getArgument(0)
            } else {
                -1
            }
        }
    }

    protected fun verifyListUpdateCallbackNotCalled() {
        verify(listUpdateCallback, never()).onChanged(anyInt(), anyInt(), anyString())
        verifyOtherListUpdateCallbackNotCalled()
    }

    /**
     * Check that [listUpdateCallback] methods except [ListUpdateCallback.onChanged] never called.
     */
    protected fun verifyOtherListUpdateCallbackNotCalled() {
        verify(listUpdateCallback, never()).onInserted(anyInt(), anyInt())
        verify(listUpdateCallback, never()).onMoved(anyInt(), anyInt())
        verify(listUpdateCallback, never()).onRemoved(anyInt(), anyInt())
    }

    /**
     * Check that [observer] never called.
     */
    protected fun verifySelectionObserverNotCalled() {
        verify(observer, never()).onSelectionStarted(selection)
        verify(observer, never()).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)
    }

    protected fun verifySelectionObserverCalledOneTime(times: Int = 1) {
        verify(observer).onSelectionStarted(selection)
        verify(observer, times(times)).onSelectionChanged(selection)
        verify(observer, never()).onSelectionStopped(selection)
    }

    protected fun verifyListUpdateCallbackCalledOneTime(position: Int = 1, count: Int = 1) {
        verify(listUpdateCallback).onChanged(position, count, SelectionTracker.SELECTION_CHANGED_MARKER)
        verifyOtherListUpdateCallbackNotCalled()
    }
}