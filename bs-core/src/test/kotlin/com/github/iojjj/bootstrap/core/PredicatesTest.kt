package com.github.iojjj.bootstrap.core

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * JUnit test suite for [Predicates] class.
 */
class PredicatesTest {

    @Test
    fun testAlwaysTrue() {
        assertSameResult(true, Predicates.ALWAYS_TRUE)
    }

    @Test
    fun testAlwaysFalse() {
        assertSameResult(false, Predicates.ALWAYS_FALSE)
    }

    private fun assertSameResult(expectedResult: Boolean, predicate: Predicate<Any?>) {
        assertEquals(expectedResult, predicate(1))
        assertEquals(expectedResult, predicate(1.2))
        assertEquals(expectedResult, predicate("String"))
        assertEquals(expectedResult, predicate(Any()))
        assertEquals(expectedResult, predicate(null))
    }
}