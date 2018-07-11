@file:JvmName("AdapterUtils")

package com.github.iojjj.bootstrap.adapters.adapter

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import androidx.core.content.withStyledAttributes
import com.github.iojjj.bootstrap.adapters.R

/**
 * Find [query] in the current `CharSequence` and return a `Spannable` with highlighted substrings.
 *
 * @param context instance of `Context`
 * @param query query string. If empty the a current `CharSequence` returned
 * @param colorId highlight color resource. Optional. If omitted, `colorAccent` will be used
 *
 * @return a `CharSequence` with highlighted substrings
 */
@JvmOverloads
fun CharSequence.highlightQuery(context: Context, query: CharSequence?, @ColorRes colorId: Int = findAccentColor(
        context)): CharSequence {
    return highlightQuery(query, ContextCompat.getColor(context, colorId))
}

/**
 * Find [query] in the current `CharSequence` and return a `Spannable` with highlighted substrings.
 *
 * @param query query string
 * @param color highlight color
 *
 * @return a `CharSequence` with highlighted substrings
 */
fun CharSequence.highlightQuery(query: CharSequence?, @ColorInt color: Int): CharSequence {
    if (TextUtils.isEmpty(query)) {
        return this
    }
    val spannableString = SpannableString(this)
    val colorSpan = ForegroundColorSpan(color)
    val filteredString = query.toString().trim().toLowerCase()
    val lowercase = this.toString().toLowerCase()
    val length = filteredString.length
    var index = -1
    var prevIndex: Int
    do {
        prevIndex = index
        index = lowercase.indexOf(filteredString, prevIndex + 1)
        if (index == -1) {
            break
        }
        spannableString.setSpan(colorSpan, index, index + length, 0)
    } while (true)
    return spannableString
}

@ColorRes
private fun findAccentColor(context: Context): Int {
    var colorId = 0
    context.withStyledAttributes(attrs = intArrayOf(R.attr.colorAccent)) {
        colorId = getResourceId(0, 0)
    }
    return colorId
}