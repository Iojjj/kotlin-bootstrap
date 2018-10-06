@file:JvmName("StringUtils")

package kotlinx

import android.os.Build
import android.text.Html

fun String.fromHtml(): CharSequence = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
} else {
    @Suppress("DEPRECATION")
    Html.fromHtml(this)
}

fun String.fromHtml(imageGetter: Html.ImageGetter, tagHandler: Html.TagHandler): CharSequence = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler)
} else {
    @Suppress("DEPRECATION")
    Html.fromHtml(this, imageGetter, tagHandler)
}