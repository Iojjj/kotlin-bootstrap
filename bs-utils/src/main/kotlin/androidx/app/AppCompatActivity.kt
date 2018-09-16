@file:Suppress("NOTHING_TO_INLINE")

package androidx.app

import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

/**
 * Find [Toolbar] by provided id and set it via [AppCompatActivity.setSupportActionBar].
 *
 * @param id toolbar id
 */
inline fun AppCompatActivity.setSupportActionBar(@IdRes id: Int) = setSupportActionBar(findViewById(id))