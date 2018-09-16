package com.github.iojjj.bootstrap.utils

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * JUnit test suite for [InvokableObservable] class.
 */
class InvokableObservableTest {

    private lateinit var observable: InvokableObservable<TestListener>
    private lateinit var testListener1: TestListener
    private lateinit var testListener2: TestListener

    /**
     * Create a new instance of [InvokableObservable] for every test.
     */
    @Before
    fun setUp() {
        observable = observableOf()
        testListener1 = mock(TestListener::class.java)
        testListener2 = mock(TestListener::class.java)
    }

    /**
     * Test for [InvokableObservable.addObserver] method.
     */
    @Test
    fun addListener() {
        assertEquals(true, observable.addObserver(testListener1))
        assertEquals(true, observable.addObserver(testListener1))
        assertEquals(true, observable.addObserver(testListener2))
    }

    /**
     * Test for [InvokableObservable.removeObserver] method.
     */
    @Test
    fun removeListener() {
        assertEquals(true, observable.addObserver(testListener1))
        assertEquals(true, observable.removeObserver(testListener1))
        assertEquals(false, observable.removeObserver(testListener1))
        assertEquals(false, observable.removeObserver(testListener2))
    }

    /**
     * Test for [InvokableObservable.notifyObservers] method.
     */
    @Test
    fun notifyListeners() {
        observable.addObserver(testListener1)
        observable.addObserver(testListener1)
        observable.addObserver(testListener2)
        observable.notifyObservers { it.increment1() }
        verify(testListener1, times(2)).increment1()
        verify(testListener2, times(1)).increment1()
        observable.notifyObservers { it.increment2() }
        verify(testListener1, times(2)).increment2()
        verify(testListener2, times(1)).increment2()
    }

    private interface TestListener {

        fun increment1()

        fun increment2()
    }
}