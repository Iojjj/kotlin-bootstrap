package com.github.iojjj.bootstrap.utils

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.concurrent.TimeUnit

/**
 * JUnit test suite for [BackPressHandler] class.
 */
class BackPressHandlerTest {

    private lateinit var testCallback: BackPressHandler.Callback

    @Before
    fun setUp() {
        testCallback = mock(BackPressHandler.Callback::class.java)
    }

    @Test
    fun onBackPressedMs() {
        val handler = BackPressHandler.newInstance(1000L, testCallback)
        testBackPressed(handler)
    }

    @Test
    fun onBackPressedUnits() {
        val handler = BackPressHandler.newInstance(1, TimeUnit.SECONDS, testCallback)
        testBackPressed(handler)
    }

    @Test
    fun wrongInitializationParams() {
        try {
            BackPressHandler.newInstance(-5, testCallback)
            fail("Initialization with negative timeout must throw an exception.")
        } catch (e: IllegalArgumentException) {
            /* ok */
        }

        try {
            BackPressHandler.newInstance(-5, TimeUnit.SECONDS, testCallback)
            fail("Initialization with negative timeout must throw an exception.")
        } catch (e: IllegalArgumentException) {
            /* ok */
        }
    }

    private fun testBackPressed(handler: BackPressHandler) {
        assertTrue(handler.onBackPressed())
        verify(testCallback, times(1)).onShowPrompt()
        Thread.sleep(1100)
        assertTrue(handler.onBackPressed())
        verify(testCallback, times(2)).onShowPrompt()
        Thread.sleep(300)
        assertFalse(handler.onBackPressed())
        verify(testCallback, times(2)).onShowPrompt()
    }
}