package com.github.iojjj.bootstrap.core

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * JUnit test suite for [Predicates] class.
 */
@Suppress("TestFunctionName")
class PredicatesTest {

    @Test
    fun ALWAYS_TRUE_expectOnlyTrue() {
        assertSameResult(true, Predicates.ALWAYS_TRUE)
    }

    @Test
    fun ALWAYS_FALSE_expectOnlyFalse() {
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