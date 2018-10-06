package com.github.iojjj.bootstrap.utils

import android.widget.EditText
import com.github.iojjj.bootstrap.test.BsRobolectricTestRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RuntimeEnvironment

/**
 * JUnit test suite for [AdvancedTextWatcher] class.
 */
@RunWith(BsRobolectricTestRunner::class)
class AdvancedTextWatcherTest {

    private lateinit var editText: EditText
    private lateinit var callback: AdvancedTextWatcher.OnTextChangedListener
    private lateinit var advancedTextWatcher: AdvancedTextWatcher

    @Before
    fun setUp() {
        editText = EditText(RuntimeEnvironment.application)
        callback = mock(AdvancedTextWatcher.OnTextChangedListener::class.java)
        advancedTextWatcher = AdvancedTextWatcher.newInstance()
        advancedTextWatcher.addObserver(callback)
    }

    @Test
    fun onTextChangedListener_whenInsertedNewText_shouldReceiveNotifications() {
        editText.addTextChangedListener(advancedTextWatcher)
        editText.setText("abc")
        verify(callback).onTextInserted("", "abc", "abc", 0)
        verify(callback).onTextChanged("", "abc")
        verify(callback, never()).onTextRemoved(anyString(), anyString(), anyString(), anyInt())
        verify(callback, never()).onTextReplaced(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt())
    }

    @Test
    fun onTextChangedListener_whenInsertedAdditionalText_shouldReceiveNotifications() {
        editText.setText("abc")
        editText.addTextChangedListener(advancedTextWatcher)
        editText.text.insert(2, "IN")
        verify(callback).onTextInserted("abc", "abINc", "IN", 2)
        verify(callback).onTextChanged("abc", "abINc")
        verify(callback, never()).onTextRemoved(anyString(), anyString(), anyString(), anyInt())
        verify(callback, never()).onTextReplaced(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt())
    }

    @Test
    fun onTextChangedListener_whenRemovedWholeText_shouldReceiveNotifications() {
        editText.setText("abc")
        editText.addTextChangedListener(advancedTextWatcher)
        editText.setText("")
        verify(callback).onTextRemoved("abc", "", "abc", 0)
        verify(callback).onTextChanged("abc", "")
        verify(callback, never()).onTextInserted(anyString(), anyString(), anyString(), anyInt())
        verify(callback, never()).onTextReplaced(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt())
    }

    @Test
    fun onTextChangedListener_whenRemovedPartOfText_shouldReceiveNotifications() {
        editText.setText("abc")
        editText.addTextChangedListener(advancedTextWatcher)
        editText.text.replace(1, 2, "")
        verify(callback).onTextRemoved("abc", "ac", "b", 1)
        verify(callback).onTextChanged("abc", "ac")
        verify(callback, never()).onTextInserted(anyString(), anyString(), anyString(), anyInt())
        verify(callback, never()).onTextReplaced(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt())
    }

    @Test
    fun onTextChangedListener_whenReplacedWholeText_shouldReceiveNotifications() {
        editText.setText("abc")
        editText.addTextChangedListener(advancedTextWatcher)
        editText.setText("abINc")
        verify(callback).onTextReplaced("abc", "abINc", "abc", "abINc", 0, 0)
        verify(callback).onTextChanged("abc", "abINc")
        verify(callback, never()).onTextRemoved(anyString(), anyString(), anyString(), anyInt())
        verify(callback, never()).onTextInserted(anyString(), anyString(), anyString(), anyInt())
    }

    @Test
    fun onTextChangedListener_whenReplacedPartOfText_shouldReceiveNotifications() {
        editText.setText("abc")
        editText.addTextChangedListener(advancedTextWatcher)
        editText.text.replace(1, 2, "IN")
        verify(callback).onTextReplaced("abc", "aINc", "b", "IN", 1, 1)
        verify(callback).onTextChanged("abc", "aINc")
        verify(callback, never()).onTextRemoved(anyString(), anyString(), anyString(), anyInt())
        verify(callback, never()).onTextInserted(anyString(), anyString(), anyString(), anyInt())
    }
}