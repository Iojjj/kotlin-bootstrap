package com.github.iojjj.bootstrap.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.support.annotation.IntRange
import android.support.v4.util.Preconditions
import java.util.concurrent.TimeUnit

/**
 * Helper class that handles back press and shows prompt message.
 *
 * Example of usage:
 * ```
 * class MainActivity: AppCompatActivity() {
 *
 *     private val backPressHandler = BackPressHandler.newInstance(2, TimeUnit.SECONDS, this::showOnBackPressPrompt)
 *
 *     ...
 *
 *     override fun onBackPressed() {
 *         if (backPressHandler.onBackPressed()) {
 *             super.onBackPressed()
 *         }
 *     }
 *
 *     private fun showOnBackPressPrompt() {
 *         // show toast or dialog
 *     }
 *
 *     ...
 * }
 * ```
 *
 * @property timeoutMs timeout in milliseconds
 * @property callback callback that will be called to show prompt message
 * @property lastTimePressed timestamp of last back press
 */
class BackPressHandler private constructor(private val timeoutMs: Long, private val callback: Callback?) : BackPressListener {

    private var lastTimePressed: Long = 0L

    companion object {

        /**
         * Create a new instance of [BackPressHandler].
         *
         * @param timeoutMs timeout in milliseconds
         * @param callback callback that will be called to show prompt message
         *
         * @return a new instance of [BackPressHandler]
         */
        @JvmStatic
        fun newInstance(@IntRange(from = 0) timeoutMs: Long, callback: Callback? = null): BackPressHandler = init(timeoutMs, callback)

        /**
         * Create a new instance of [BackPressHandler].
         *
         * @param timeoutMs timeout in milliseconds
         * @param callback callback that will be called to show prompt message
         *
         * @return a new instance of [BackPressHandler]
         */
        @JvmStatic
        fun newInstance(@IntRange(from = 0) timeoutMs: Long, callback: (() -> Unit)? = null): BackPressHandler = init(timeoutMs, toCallback(callback))

        /**
         * Create a new instance of [BackPressHandler].
         *
         * @param timeout timeout in specific time units
         * @param unit time units
         * @param callback callback that will be called to show prompt message
         *
         * @return a new instance of [BackPressHandler]
         */
        @JvmStatic
        fun newInstance(@IntRange(from = 0) timeout: Long, unit: TimeUnit, callback: Callback? = null): BackPressHandler =
                init(TimeUnit.MILLISECONDS.convert(timeout, unit), callback)

        /**
         * Create a new instance of [BackPressHandler].
         *
         * @param timeout timeout in specific time units
         * @param unit time units
         * @param callback callback that will be called to show prompt message
         *
         * @return a new instance of [BackPressHandler]
         */
        @JvmStatic
        fun newInstance(@IntRange(from = 0) timeout: Long, unit: TimeUnit, callback: (() -> Unit)? = null): BackPressHandler =
                init(TimeUnit.MILLISECONDS.convert(timeout, unit), toCallback(callback))

        @SuppressLint("RestrictedApi")
        private fun init(@IntRange(from = 0) timeoutMs: Long, callback: Callback?): BackPressHandler {
            Preconditions.checkArgumentInRange(timeoutMs, 0, Long.MAX_VALUE, "timeout")
            return BackPressHandler(timeoutMs, callback)
        }

        /**
         * Convert `Function0` to [Callback].
         */
        private fun toCallback(callback: (() -> Unit)?): Callback? {
            return if (callback != null) {
                object : Callback {
                    override fun onShowPrompt() {
                        callback()
                    }
                }
            } else {
                null
            }
        }
    }

    /**
     * This method must be called from [Activity.onBackPressed].
     *
     * @return `true` if prompt message has been shown, `false` otherwise
     */
    override fun onBackPressed(): Boolean {
        val prevTime = lastTimePressed
        val curTime = System.currentTimeMillis()
        val diffTime = curTime - lastTimePressed
        lastTimePressed = curTime
        if (diffTime <= timeoutMs && prevTime != 0L) {
            return false
        }
        callback?.onShowPrompt()
        return true
    }

    /**
     * Callback that will be called to show prompt message
     */
    @FunctionalInterface
    interface Callback {

        /**
         * Invoked when it is time to show prompt message.
         */
        fun onShowPrompt()
    }
}