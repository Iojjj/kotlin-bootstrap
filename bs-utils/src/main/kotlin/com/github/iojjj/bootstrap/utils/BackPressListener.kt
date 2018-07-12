package com.github.iojjj.bootstrap.utils

/**
 * Listener called when user presses back button.
 *
 * The contract of this listener: return `true` if back press has been consumed and no additional action should be performed; return `false`
 * if default behavior must be executed (e.g. Activity's original `onBackPress` method.
 */
@FunctionalInterface
interface BackPressListener {

    /**
     * Called when user presses back button
     *
     * @return `true` if back press has been consumed, `false` otherwise
     */
    fun onBackPressed(): Boolean
}