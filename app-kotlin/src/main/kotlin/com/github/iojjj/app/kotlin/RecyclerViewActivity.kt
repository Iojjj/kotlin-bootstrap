package com.github.iojjj.app.kotlin

import android.annotation.SuppressLint
import android.arch.core.executor.ArchTaskExecutor
import android.arch.paging.DataSource
import android.arch.paging.PositionalDataSource
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.app.setSupportActionBar
import com.github.iojjj.app.core.BaseRecyclerViewActivity
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter
import com.github.iojjj.bootstrap.adapters.data.ConfigurableLiveData
import com.github.iojjj.bootstrap.adapters.data.Configuration
import com.github.iojjj.bootstrap.adapters.selection.selections.Selection
import com.github.iojjj.bootstrap.adapters.selection.trackers.SelectionTracker
import com.github.iojjj.bootstrap.utils.BackPressHandler
import java.util.concurrent.TimeUnit

@SuppressLint("RestrictedApi")
class RecyclerViewActivity : BaseRecyclerViewActivity() {

    private val backPressHandler = BackPressHandler.newInstance(2, TimeUnit.SECONDS, this::showOnBackPressPrompt)
    private val adapter: PagedAdapter<String> by lazy {
        val liveData = ConfigurableLiveData.ofPagedList<Int, String>()
                .withDataSourceFactory(StringDataSource.Factory)
                .withPageSize(PAGE_SIZE)
                .withFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .build()
        return@lazy PagedAdapter.newBuilderWith(liveData)
                .withSimplePlaceholderType(R.layout.list_item_placeholder)
                .withItemType(R.layout.list_item_string, String::class)
                // when configuration changes (by any reason) this marker will be sent to ViewHolder's bind methods
                // this is useful when you want to highlight a search query
                .withOnLiveDataInvalidatedMarker(MARKER_DATA_SOURCE)
                .withOnItemClickListener { adapter, _, i, _ ->
                    if (isSelectionStarted) {
                        adapter.selectionTracker.toggle(adapter[i]!!)
                    }
                }
                .withOnItemLongClickListener { adapter, _, i, _ ->
                    val shouldSelect = !isSelectionStarted
                    if (shouldSelect) {
                        adapter.selectionTracker.toggle(adapter[i]!!)
                    }
                    return@withOnItemLongClickListener shouldSelect
                }
                .withSelectionTracker(SelectionTracker.newBuilder<String>()
                        .withMultipleSelection()
                        .withSelectionPredicate { it != "Item 1" }
                        .addObserver(object : SelectionTracker.SelectionAdapter<String>() {
                            override fun onSelectionStarted(selection: Selection<String>) = startSelection()
                            override fun onSelectionChanged(selection: Selection<String>) = setActionModeTitle(selection.size.toString())
                            override fun onSelectionStopped(selection: Selection<String>) = stopSelection()
                        })
                )
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.onRestoreInstanceState(savedInstanceState)
    }

    override fun initToolbar(layoutId: Int) {
        setSupportActionBar(R.id.toolbar)
    }

    override fun initList(list: RecyclerView) {
        super.initList(list)
//        list.addItemDecoration(paddingDecorationOf(
//                paddingStart = resources.getDimensionPixelSize(R.dimen.list_item_padding_start),
//                paddingEnd = resources.getDimensionPixelSize(R.dimen.list_item_padding_end))
//        )
        list.adapter = adapter
        adapter.observe(this)
    }

    override fun filter(query: String?) = adapter.filter(query)

    override fun onActionModeItemDeleteClicked() {
        val removed: MutableList<String> = adapter.configuration.getOrDefault(KEY_REMOVED) { mutableListOf() }
        removed.addAll(adapter.selectionTracker.selection.snapshot())
        adapter.configuration.notifyConfigurationChanged()
    }

    override fun onActionModeDestroyed() {
        adapter.selectionTracker.clear()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        adapter.onSaveInstanceState(outState!!)
    }

    override fun onBackPressed() {
        if (!backPressHandler.onBackPressed()) {
            super.onBackPressed()
        }
    }

    private fun showOnBackPressPrompt() {
        val builder = SpannableStringBuilder("Press Back again to close the app.")
        val start = builder.indexOf("Back")
        val end = start + "Back".length
        builder.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        Toast.makeText(this, builder, Toast.LENGTH_SHORT).show()
    }

    private class StringDataSource(private val data: List<String>) : PositionalDataSource<String>() {

        companion object Factory : ConfigurableLiveData.Factory<DataSource<Int, String>> {

            private val data: List<String> by lazy { (0..DATA_SET_SIZE).map { "Item ${(it + 1)}" } }

            override fun create(configuration: Configuration): StringDataSource {
                val removed: List<String>? = configuration[KEY_REMOVED]
                val filter: String? = configuration[PagedAdapter.CONFIG_KEY_FILTER]
                return if (filter == null && (removed == null || removed.isEmpty())) {
                    StringDataSource(data)
                } else {
                    val stringFilter: (String) -> Boolean =
                            { (filter == null || it.contains(filter, true)) && (removed == null || !removed.contains(it)) }
                    val filteredData = data.filter(stringFilter)
                    StringDataSource(filteredData)
                }
            }
        }

        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<String>) {
            val size = data.size
            val startPosition = computeInitialLoadPosition(params, size)
            val loadSize = computeInitialLoadSize(params, startPosition, size)
            callback.onResult(data.subList(startPosition, loadSize), startPosition, size)
        }

        override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<String>) {
            callback.onResult(data.subList(params.startPosition, params.startPosition + params.loadSize))
        }
    }
}
