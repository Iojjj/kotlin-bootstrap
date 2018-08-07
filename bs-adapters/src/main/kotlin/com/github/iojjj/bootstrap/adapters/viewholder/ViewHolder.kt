package com.github.iojjj.bootstrap.adapters.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter

/**
 * ViewHolder interface that must be implemented by views.
 */
@Suppress("unused")
interface ViewHolder<in T> {

    /**
     * Factory that creates stub view holders that do nothing.
     */
    companion object Factory : PagedAdapter.ViewHolderFactory {
        override fun create(view: View): RecyclerView.ViewHolder {
            return StubViewHolder(view)
        }
    }

    /**
     * Called when `ViewHolder` has just been created.
     *
     * @param adapter adapter that created a view holder
     */
    fun onCreated(adapter: PagedAdapter<*>) {
        /* no-op */
    }

    /**
     * Bind item to view.
     *
     * @param adapter adapter that stores item
     * @param item some item
     */
    fun bind(adapter: PagedAdapter<*>, position: Int, item: T)

    /**
     * Bind item to view. Any class that implements this interface and don't cares about payload must call [bind] method.
     *
     * @param adapter adapter that stores item
     * @param item some item
     * @param payload list of payloads that triggered binding operation
     */
    fun bind(adapter: PagedAdapter<*>, position: Int, item: T, payload: MutableList<Any>?) = bind(adapter, position, item)

    /**
     * Default implementation of [ViewHolder] for [RecyclerView]'s adapter.
     */
    abstract class RecyclerViewViewHolder<in T>(view: View) : RecyclerView.ViewHolder(view), ViewHolder<T> {

        override fun onCreated(adapter: PagedAdapter<*>) {
            /* no-op */
        }

        override fun bind(adapter: PagedAdapter<*>, position: Int, item: T) {
            /* no-op */
        }

        override fun bind(adapter: PagedAdapter<*>, position: Int, item: T, payload: MutableList<Any>?) {
            bind(adapter, position, item)
        }
    }

    private class StubViewHolder(view: View) : RecyclerViewViewHolder<Any>(view)
}

@Suppress("UNCHECKED_CAST")
internal fun <T> ViewHolder<*>.cast(): ViewHolder<T> = this as ViewHolder<T>