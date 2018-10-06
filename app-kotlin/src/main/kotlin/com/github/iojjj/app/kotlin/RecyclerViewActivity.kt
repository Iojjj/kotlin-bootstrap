package com.github.iojjj.app.kotlin

import android.annotation.SuppressLint
import android.arch.core.executor.ArchTaskExecutor
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import androidx.app.setSupportActionBar
import com.github.iojjj.app.core.BaseRecyclerViewActivity
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter
import com.github.iojjj.bootstrap.adapters.data.ConfigLiveData
import com.github.iojjj.bootstrap.adapters.selection.selections.Selection
import com.github.iojjj.bootstrap.adapters.selection.trackers.SelectionTracker
import com.github.iojjj.bootstrap.utils.BackPressHandler
import kotlinx.fromHtml
import java.util.concurrent.TimeUnit

@SuppressLint("RestrictedApi")
class RecyclerViewActivity : BaseRecyclerViewActivity() {

    private val backPressHandler = BackPressHandler.newInstance(2, TimeUnit.SECONDS, this::showOnBackPressPrompt)
    private val adapter: PagedAdapter<String> by lazy {
        val liveData = ConfigLiveData.ofPagedList<Int, String>()
                .withDataSourceFactory(StringDataSource.DataFactory)
                .withPageSize(PAGE_SIZE)
                .withFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .withNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
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
                            override fun onSelectionChanged(selection: Selection<String>) {
                                if (!selection.isEmpty()) {
                                    setActionModeTitle(selection.size.toString())
                                }
                            }

                            override fun onSelectionStopped(selection: Selection<String>) = stopSelection()
                        })
                )
                .build()
    }

    private var toast: Toast? = null

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
        adapter.liveData.invalidate()
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
        toast?.cancel()
        toast = Toast.makeText(this, "Press <b>Back</b> again to close the app.".fromHtml(), Toast.LENGTH_SHORT)
        toast!!.show()
    }

}
