package com.github.iojjj.app.kotlin

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.github.iojjj.app.core.BaseStringView
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter
import com.github.iojjj.bootstrap.adapters.adapter.highlightQuery

class StringView : BaseStringView {

    constructor(context: Context) : super(context, "Kotlin")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, "Kotlin")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr, "Kotlin")

    override fun bind(adapter: PagedAdapter<*>, position: Int, item: String) {
        if (adapter.isFiltered) {
            updateText(item.highlightQuery(adapter.getFilterQuery(), Color.RED))
        } else {
            updateText(item)
        }
        isActivated = adapter.toTyped<String>().selectionTracker.selection.contains(item)
    }
}