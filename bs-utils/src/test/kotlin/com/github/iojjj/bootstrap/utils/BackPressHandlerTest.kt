package com.github.iojjj.bootstrap.utils

import junit.framework.Assert.fail
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * JUnit test suite for [BackPressHandler] class.
 */
class BackPressHandlerTest {

    private lateinit var testCallback: TestCallback

    @Before
    fun setUp() {
        testCallback = TestCallback()
    }

    @Test
    fun onBackPressedMs() {
        val handler = BackPressHandler.newInstance(1000L, testCallback)
        assertFalse(handler.onBackPressed())
        assertTrue(testCallback.showPromptCalled)
        testCallback.reset()
        Thread.sleep(1100)
        assertFalse(handler.onBackPressed())
        assertTrue(testCallback.showPromptCalled)
        testCallback.reset()
        Thread.sleep(300)
        assertTrue(handler.onBackPressed())
        assertFalse(testCallback.showPromptCalled)
        testCallback.reset()
    }

    @Test
    fun onBackPressedUnits() {
        val handler = BackPressHandler.newInstance(1, TimeUnit.SECONDS, testCallback)
        assertFalse(handler.onBackPressed())
        assertTrue(testCallback.showPromptCalled)
        testCallback.reset()
        Thread.sleep(1100)
        assertFalse(handler.onBackPressed())
        assertTrue(testCallback.showPromptCalled)
        testCallback.reset()
        Thread.sleep(300)
        assertTrue(handler.onBackPressed())
        assertFalse(testCallback.showPromptCalled)
        testCallback.reset()
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

    private class TestCallback : BackPressHandler.Callback {

        var showPromptCalled = false

        override fun onShowPrompt() {
            showPromptCalled = true
        }

        fun reset() {
            showPromptCalled = false
        }
    }
}