package com.github.iojjj.bootstrap.utils

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * JUnit test suite for [InvokableObservable] class.
 */
class ObservableTest {

    private lateinit var observable: InvokableObservable<TestListener>

    /**
     * Create a new instance of [InvokableObservable] for every test.
     */
    @Before
    fun setUp() {
        observable = observableOf()
    }

    /**
     * Test for [InvokableObservable.addObserver] method.
     */
    @Test
    fun addListener() {
        val testListener1 = TestListener()
        val testListener2 = TestListener()
        assertEquals(true, observable.addObserver(testListener1))
        assertEquals(true, observable.addObserver(testListener1))
        assertEquals(true, observable.addObserver(testListener2))
    }

    /**
     * Test for [InvokableObservable.removeObserver] method.
     */
    @Test
    fun removeListener() {
        val testListener1 = TestListener()
        val testListener2 = TestListener()
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
        val testListener1 = TestListener()
        val testListener2 = TestListener()
        observable.addObserver(testListener1)
        observable.addObserver(testListener1)
        observable.addObserver(testListener2)
        observable.notifyObservers { it.increment1() }
        assertEquals(2, testListener1.value1)
        assertEquals(1, testListener2.value1)
        observable.notifyObservers { it.increment2() }
        assertEquals(2, testListener1.value2)
        assertEquals(1, testListener2.value2)
    }

    private class TestListener {

        internal var value1: Int = 0
        internal var value2: Int = 0

        fun increment1() = value1++

        fun increment2() = value2++
    }
}