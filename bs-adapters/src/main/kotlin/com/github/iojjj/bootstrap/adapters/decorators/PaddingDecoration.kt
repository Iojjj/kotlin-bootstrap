@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.github.iojjj.bootstrap.adapters.decorators

import android.graphics.Rect
import android.os.Build
import android.support.annotation.IntDef
import android.support.annotation.IntRange
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * Item padding decoration. Compatible with [LinearLayoutManager] and [GridLayoutManager] only.
 */
class PaddingDecoration private constructor(@IntRange(from = 0) private val paddingStart: Int,
                                            @IntRange(from = 0) private val paddingTop: Int,
                                            @IntRange(from = 0) private val paddingEnd: Int,
                                            @IntRange(from = 0) private val paddingBottom: Int,
                                            @IntRange(from = 1) private val spanCount: Int,
                                            @Orientation private val orientation: Int) : RecyclerView.ItemDecoration() {

    private val paddingTopHalf: Int = paddingTop shr 1
    private val paddingBottomHalf: Int = paddingBottom shr 1
    private val paddingStartHalf: Int = paddingStart shr 1
    private val paddingEndHalf: Int = paddingEnd shr 1

    companion object {

        const val HORIZONTAL = RecyclerView.HORIZONTAL
        const val VERTICAL = RecyclerView.VERTICAL

        /**
         * Create a new instance of [Builder].
         *
         * @return a new instance of [Builder]
         */
        @JvmStatic
        fun newBuilder(): Builder = Builder()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter ?: return
        val totalCount = adapter.itemCount
        val position = parent.getChildAdapterPosition(view)
        var lastItemsCount = totalCount.rem(spanCount)
        if (lastItemsCount == 0) {
            lastItemsCount = spanCount
        }
        // set values depending on layout direction
        val curStart: Int
        val curEnd: Int
        val curHalfStart: Int
        val curHalfEnd: Int
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || parent.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
            curStart = paddingStart
            curEnd = paddingEnd
            curHalfStart = paddingStartHalf
            curHalfEnd = paddingEndHalf
        } else {
            curStart = paddingEnd
            curEnd = paddingStart
            curHalfStart = paddingEndHalf
            curHalfEnd = paddingStartHalf
        }

        val left: Int
        val right: Int
        val top: Int
        val bottom: Int

        // fX -> mX -> lX

        if (orientation == HORIZONTAL) {
            // span count = 4
            // L   X   X   X   X   R
            // f00 f10 f20 f30 f40 f50 T
            // f01 f11 f21 f31 f41 f51 Y
            // f02 f12 f22 f32 f42 f52 Y
            // f03 f13 f23 f33 f43 f53 B

            when {
                position < spanCount -> { // T items
                    left = curStart
                    right = curHalfEnd
                }
                position >= totalCount - lastItemsCount -> { // B items
                    left = curHalfStart
                    right = curEnd
                }
                else -> { // Y items
                    left = curHalfStart
                    right = curHalfEnd
                }
            }

            when {
                position.rem(spanCount) == 0 -> { // L items
                    top = paddingTop
                    bottom = if (spanCount > 1) paddingBottomHalf else paddingBottom
                }
                position.rem(spanCount) == spanCount - 1 -> { // R items
                    top = if (spanCount > 1) paddingTopHalf else paddingTop
                    bottom = paddingBottom
                }
                else -> { // X items
                    top = paddingTopHalf
                    bottom = paddingBottomHalf
                }
            }
        } else {
            // span count = 4
            // L   X   X   R
            // f00 f01 f02 f03 T
            // f10 f11 f12 f13 Y
            // f20 f21 f22 f23 Y
            // f30 f31 f32 f33 Y
            // f40 f41 f42 f43 Y
            // f50 f51 f52 f53 Y
            // f60 f61 f62 f63 B

            when {
                position.rem(spanCount) == 0 -> { // L items
                    left = curStart
                    right = if (spanCount > 1) curHalfEnd else curEnd
                }
                position.rem(spanCount) == spanCount - 1 -> { // R items
                    left = if (spanCount > 1) curHalfStart else curStart
                    right = curEnd
                }
                else -> { // X items
                    left = curHalfStart
                    right = curHalfEnd
                }
            }

            when {
                position < spanCount -> { // T items
                    top = paddingTop
                    bottom = paddingBottomHalf
                }
                position >= totalCount - lastItemsCount -> { // B items
                    top = paddingTopHalf
                    bottom = paddingBottom
                }
                else -> { // Y items
                    top = paddingTopHalf
                    bottom = paddingBottomHalf
                }
            }
        }
        outRect.set(left, top, right, bottom)
    }

    class Builder internal constructor() {
        private var paddingStart: Int = 0
        private var paddingTop: Int = 0
        private var paddingEnd: Int = 0
        private var paddingBottom: Int = 0
        private var spanCount: Int = 1
        private var orientation: Int = VERTICAL

        /**
         * Set all paddings to provided value.
         *
         * @param padding value for paddings
         *
         * @return same builder for chaining calls
         */
        fun withPadding(@IntRange(from = 0) padding: Int): Builder {
            paddingStart = padding
            paddingEnd = padding
            paddingTop = padding
            paddingBottom = padding
            return this
        }

        /**
         * Set horizontal (`start`, `end`) paddings to provided value.
         *
         * @param padding value for paddings
         *
         * @return same builder for chaining calls
         */
        fun withHorizontalPadding(@IntRange(from = 0) padding: Int): Builder {
            paddingStart = padding
            paddingEnd = padding
            return this
        }

        /**
         * Set vertical (`top`, `bottom`) paddings to provided value.
         *
         * @param padding value for paddings
         *
         * @return same builder for chaining calls
         */
        fun withVerticalPadding(@IntRange(from = 0) padding: Int): Builder {
            paddingTop = padding
            paddingBottom = padding
            return this
        }

        /**
         * Set `start` padding to provided value.
         *
         * @param padding value for padding
         *
         * @return same builder for chaining calls
         */
        fun withStartPadding(@IntRange(from = 0) padding: Int): Builder {
            paddingStart = padding
            return this
        }

        /**
         * Set `end` padding to provided value.
         *
         * @param padding value for padding
         *
         * @return same builder for chaining calls
         */
        fun withEndPadding(@IntRange(from = 0) padding: Int): Builder {
            paddingEnd = padding
            return this
        }

        /**
         * Set `top` padding to provided value.
         *
         * @param padding value for padding
         *
         * @return same builder for chaining calls
         */
        fun withTopPadding(@IntRange(from = 0) padding: Int): Builder {
            paddingTop = padding
            return this
        }

        /**
         * Set `bottom` padding to provided value.
         *
         * @param padding value for padding
         *
         * @return same builder for chaining calls
         */
        fun withBottomPadding(@IntRange(from = 0) padding: Int): Builder {
            paddingBottom = padding
            return this
        }

        /**
         * Set number of spans (columns or rows) that will be created by `LayoutManager`. Default values is `1`.
         *
         * @param count number of spans (columns or row)
         *
         * @return same builder for chaining calls
         */
        fun withSpanCount(@IntRange(from = 1) count: Int): Builder {
            spanCount = count
            return this
        }

        /**
         * Set orientation of `LayoutManager`. Default values is [VERTICAL].
         *
         * @param orientation orientation of `LayoutManager`
         *
         * @return same builder for chaining calls
         */
        fun withOrientation(@Orientation orientation: Int): Builder {
            this.orientation = orientation
            return this
        }

        /**
         * Create a new instance of [PaddingDecoration] initialized with settings from this builder.
         *
         * @return a new instance of [PaddingDecoration]
         */
        fun build(): PaddingDecoration {
            return PaddingDecoration(paddingStart, paddingTop, paddingEnd, paddingBottom, spanCount, orientation)
        }
    }

    /**
     * Orientation of [LinearLayoutManager] or [GridLayoutManager].
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(value = [HORIZONTAL, VERTICAL])
    annotation class Orientation
}

/**
 * Create a new instance of [PaddingDecoration] using provided settings.
 *
 * @param paddingStart `start` padding value
 * @param paddingStart `end` padding value
 * @param paddingStart `top` padding value
 * @param paddingStart `bottom` padding value
 * @param spanCount number of spans (columns or row)
 * @param orientation orientation of `LayoutManager`
 *
 * @return a new instance of [PaddingDecoration]
 */
inline fun paddingDecorationOf(@IntRange(from = 0) paddingStart: Int = 0,
                               @IntRange(from = 0) paddingTop: Int = 0,
                               @IntRange(from = 0) paddingEnd: Int = 0,
                               @IntRange(from = 0) paddingBottom: Int = 0,
                               @IntRange(from = 1) spanCount: Int = 1,
                               @PaddingDecoration.Orientation orientation: Int = PaddingDecoration.VERTICAL): PaddingDecoration {
    return PaddingDecoration.newBuilder()
            .withStartPadding(paddingStart)
            .withEndPadding(paddingEnd)
            .withTopPadding(paddingTop)
            .withBottomPadding(paddingBottom)
            .withSpanCount(spanCount)
            .withOrientation(orientation)
            .build()
}

/**
 * Create a new instance of [PaddingDecoration] using provided settings.
 *
 * @param paddingHorizontal `start` and `end` padding value
 * @param paddingVertical `top` and `bottom` padding value
 * @param spanCount number of spans (columns or row)
 * @param orientation orientation of `LayoutManager`
 *
 * @return a new instance of [PaddingDecoration]
 */
inline fun paddingDecorationOf(@IntRange(from = 0) paddingHorizontal: Int = 0,
                               @IntRange(from = 0) paddingVertical: Int = 0,
                               @IntRange(from = 1) spanCount: Int = 1,
                               @PaddingDecoration.Orientation orientation: Int = PaddingDecoration.VERTICAL): PaddingDecoration {
    return PaddingDecoration.newBuilder()
            .withHorizontalPadding(paddingHorizontal)
            .withVerticalPadding(paddingVertical)
            .withSpanCount(spanCount)
            .withOrientation(orientation)
            .build()
}

/**
 * Create a new instance of [PaddingDecoration] using provided settings.
 *
 * @param padding all padding value
 * @param spanCount number of spans (columns or row)
 * @param orientation orientation of `LayoutManager`
 *
 * @return a new instance of [PaddingDecoration]
 */
inline fun paddingDecorationOf(@IntRange(from = 0) padding: Int = 0,
                               @IntRange(from = 1) spanCount: Int = 1,
                               @PaddingDecoration.Orientation orientation: Int = PaddingDecoration.VERTICAL): PaddingDecoration {
    return PaddingDecoration.newBuilder()
            .withPadding(padding)
            .withSpanCount(spanCount)
            .withOrientation(orientation)
            .build()
}