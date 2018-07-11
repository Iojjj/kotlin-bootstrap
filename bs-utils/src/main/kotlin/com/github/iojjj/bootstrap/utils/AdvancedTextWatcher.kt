package com.github.iojjj.bootstrap.utils

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher

/**
 * Implementation of [TextWatcher] that invokes more user-friendly callback methods.
 *
 * @property observable instance of `Observable` that will store listeners
 * @property beforeText text that is being replaced
 * @property beforeStart start position in [beforeText]
 * @property afterText text that replaced [beforeText]
 * @property afterStart start position in [afterText]
 * @property insertedText text that was inserted
 * @property replacedText text that was replaced
 */
class AdvancedTextWatcher private constructor(
        private val observable: InvokableObservable<OnTextChangedListener> = observableOf()
) :
        TextWatcher,
        Observable<AdvancedTextWatcher.OnTextChangedListener> by observable {

    private var beforeText: String? = null
    private var beforeStart: Int = 0
    private var afterText: String? = null
    private var afterStart: Int = 0
    private var insertedText: String? = null
    private var replacedText: String? = null

    companion object {

        /**
         * Create a new instance of [AdvancedTextWatcher].
         *
         * @return a new instance of [AdvancedTextWatcher]
         */
        @JvmStatic
        fun newInstance(): AdvancedTextWatcher {
            return AdvancedTextWatcher()
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        beforeText = s.toString()
        replacedText = beforeText!!.substring(start, start + count)
        beforeStart = start
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        afterText = s.toString()
        insertedText = afterText!!.substring(start, start + count)
        afterStart = start
    }

    override fun afterTextChanged(s: Editable) {
        when {
        // no text was replaced, this is an insertion
            TextUtils.isEmpty(replacedText) -> observable.notifyObservers({
                it.onTextInserted(beforeText!!, afterText!!, insertedText!!, beforeStart)
            })
        // no text was inserted, this is removal
            TextUtils.isEmpty(insertedText) -> observable.notifyObservers({
                it.onTextRemoved(beforeText!!, afterText!!, replacedText!!, beforeStart)
            })
        // this is replacement
            else -> observable.notifyObservers({
                it.onTextReplaced(beforeText!!, afterText!!, replacedText!!, insertedText!!, beforeStart, afterStart)
            })
        }
        // text was changed
        if (!TextUtils.equals(beforeText, afterText)) {
            observable.notifyObservers { it.onTextChanged(beforeText!!, afterText!!) }
        }
    }

    /**
     * Listener that will be called when any text change will be detected.
     */
    interface OnTextChangedListener {

        /**
         * Called when a new portion of text was inserted.
         *
         * @param prevText previous string
         * @param newText new string
         * @param insertedText inserted string
         * @param insertedPosition position of [insertedText] in [newText]
         */
        fun onTextInserted(prevText: String, newText: String, insertedText: String, insertedPosition: Int)

        /**
         * Called when a new portion of text replaced portion of old text.
         *
         * @param prevText previous string
         * @param newText new string
         * @param removedText string that was replaced by [insertedText]
         * @param removedPosition position of [removedText] in [prevText]
         * @param insertedText string that was inserted instead of [removedText]
         * @param insertedPosition position of [insertedText] in [newText]
         */
        fun onTextReplaced(prevText: String, newText: String, removedText: String, insertedText: String, removedPosition: Int, insertedPosition: Int)

        /**
         * Called when a portion of text was removed.
         *
         * @param prevText previous string
         * @param newText new string
         * @param removedText string that was removed from [prevText]
         * @param removedPosition position of [removedText] in [prevText]
         */
        fun onTextRemoved(prevText: String, newText: String, removedText: String, removedPosition: Int)

        /**
         * Called when any text changes were detected.
         *
         * @param prevText previous string
         * @param newText new string
         */
        fun onTextChanged(prevText: String, newText: String)
    }

    /**
     * This adapter class provides empty implementations of the methods from [OnTextChangedListener].
     * Any custom listener that cares only about a subset of the methods of this listener can
     * simply subclass this adapter class instead of implementing the interface directly.
     */
    abstract class OnTextChangedAdapter : OnTextChangedListener {

        override fun onTextInserted(prevText: String, newText: String, insertedText: String, insertedPosition: Int) {

        }

        override fun onTextReplaced(prevText: String, newText: String, removedText: String, insertedText: String, removedPosition: Int,
                                    insertedPosition: Int) {
        }

        override fun onTextRemoved(prevText: String, newText: String, removedText: String, removedPosition: Int) {
        }

        override fun onTextChanged(prevText: String, newText: String) {

        }
    }
}
