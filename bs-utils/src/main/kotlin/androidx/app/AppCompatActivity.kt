@file:Suppress("NOTHING_TO_INLINE")

package androidx.app

import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity

inline fun AppCompatActivity.setSupportActionBar(@IdRes id: Int) = setSupportActionBar(findViewById(id))