package com.github.iojjj.bootstrap.adapters.adapter

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v7.util.AdapterListUpdateCallback
import android.support.v7.util.BatchingListUpdateCallback
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.set
import androidx.core.util.valueIterator
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter.ViewHolderFactory
import com.github.iojjj.bootstrap.adapters.data.ConfigLiveData
import com.github.iojjj.bootstrap.adapters.data.Configuration
import com.github.iojjj.bootstrap.adapters.data.OnInvalidatedObserver
import com.github.iojjj.bootstrap.adapters.selection.trackers.*
import com.github.iojjj.bootstrap.adapters.viewholder.Placeholder
import com.github.iojjj.bootstrap.adapters.viewholder.PlaceholderViewHolder
import com.github.iojjj.bootstrap.adapters.viewholder.ViewHolder
import com.github.iojjj.bootstrap.adapters.viewholder.cast
import com.github.iojjj.bootstrap.utils.Predicate
import com.github.iojjj.bootstrap.utils.StateAware
import org.jetbrains.annotations.Contract
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

typealias VhFactory = (View) -> RecyclerView.ViewHolder
typealias IdMapper<T> = (Int, T?) -> Long
typealias OnClickListener<T> = (PagedAdapter<T>, View, Int, Long) -> Unit
typealias OnLongClickListener<T> = (PagedAdapter<T>, View, Int, Long) -> Boolean

@Suppress("MemberVisibilityCanBePrivate")
/**
 * Implementation of [PagedListAdapter] with additional utils and easy-to-use builder.
 *
 * @property liveData `LiveData` used to fill an adapter.
 * @property itemDataMap Map that holds item related data like [ViewHolderFactory] or item type.
 * @property itemIdMapper Mapper used for conversion of items to their IDs.
 * @property onItemClickListener Listener that will be set on item's root view.
 * @property onItemLongClickListener Listener that will be set on item's root view.
 * @property invalidationMarker Marker that will be sent when `LiveData` invalidates.
 * @property selectionTracker Tracker used to select items in adapter.
 */
class PagedAdapter<T> private constructor(
        val liveData: ConfigLiveData<PagedList<T>?>,
        private val itemDataMap: SparseArray<ItemData>,
        private val itemIdMapper: IdMapper<T>?,
        private val onItemClickListener: OnClickListener<T>?,
        private val onItemLongClickListener: OnLongClickListener<T>?,
        private val invalidationMarker: Any?,
        selectionTrackerBuilder: SelectionTracker.StageObservers<T>?,
        diffItemCallback: DiffUtil.ItemCallback<T>)

    :
        PagedListAdapter<T, RecyclerView.ViewHolder>(diffItemCallback),
        StateAware {

    companion object {

        /**
         * Key used to get filter query from [Configuration].
         */
        const val CONFIG_KEY_FILTER = "com.github.iojjj.bootstrap.adapters.adapter.FILTER"

        private const val TYPE_PLACEHOLDER = -1

        /**
         * Create a new instance of builder that help to configure [PagedAdapter].
         *
         * @param T type of items
         *
         * @return a new instance of builder
         */
        @JvmStatic
        fun <T> newBuilder(): StageDataSource<T> = Builder()

        /**
         * Create a new instance of builder that help to configure [PagedAdapter].
         *
         * @param liveData instance of [ConfigLiveData] passed to the builder
         * @param T type of items
         *
         * @return a new instance of builder
         */
        @JvmStatic
        fun <T> newBuilderWith(liveData: ConfigLiveData<PagedList<T>?>): StageOptional<T> = newBuilder<T>().withLiveData(liveData)
    }

    val configuration: Configuration
        get() = liveData.configuration

    /**
     * Flag indicates that adapter is filtered.
     *
     * @see filter
     * @see getFilterQuery
     */
    val isFiltered: Boolean
        get() = getFilterQuery<Any?>() != null

    val selectionTracker: SelectionTracker<T>

    private val liveDataObserver by lazy { Observer(this::submitList) }

    init {
        setHasStableIds(itemIdMapper != null)
        if (invalidationMarker != null) {
            liveData.onInvalidatedObservable.addObserver(OnInvalidatedObserver {
                notifyItemRangeChanged(0, itemCount, invalidationMarker)
            })
        }
        selectionTracker = selectionTrackerBuilder
                ?.withPositionMapper { indexOf(it) }
                ?.withListUpdateCallback(BatchingListUpdateCallback(AdapterListUpdateCallback(this)))
                ?.build()
                ?: StubSelectionTracker.cast()
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                selectionTracker.checkSelection()
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val container = itemDataMap[viewType]
                ?: throw NoSuchElementException(
                        "Can't instantiate a view holder for item type ${if (viewType == TYPE_PLACEHOLDER) "PLACEHOLDER" else viewType.toString()}")
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder = container.provideViewHolder(inflater, parent)
        if (viewHolder is ViewHolder<*>) {
            viewHolder.onCreated(this)
        } else {
            throw IllegalArgumentException("ViewHolder must extend ${ViewHolder::class.qualifiedName} interface.")
        }
        onItemClickListener?.let { listener ->
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.adapterPosition
                listener(this, it, position, getItemId(position))
            }
        }
        onItemLongClickListener?.let { listener ->
            viewHolder.itemView.setOnLongClickListener {
                val position = viewHolder.adapterPosition
                return@setOnLongClickListener listener(this, it, position, getItemId(position))
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as? ViewHolder<*>
                ?: throw IllegalArgumentException("ViewHolder must extend ${ViewHolder::class.qualifiedName} interface.")
        if (holder.itemViewType == TYPE_PLACEHOLDER) {
            viewHolder.cast<Placeholder>().bind(this, position, Placeholder)
        } else {
            viewHolder.cast<T>().bind(this, position, getItem(position)!!)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val viewHolder = holder as? ViewHolder<*>
                ?: throw IllegalArgumentException("ViewHolder must extend ${ViewHolder::class.qualifiedName} interface.")
        if (holder.itemViewType == TYPE_PLACEHOLDER) {
            viewHolder.cast<Placeholder>().bind(this, position, Placeholder, payloads)
        } else {
            viewHolder.cast<T>().bind(this, position, getItem(position)!!, payloads)
        }
    }

    @Contract(pure = true)
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position) ?: return TYPE_PLACEHOLDER
        val itemData = itemDataMap.valueIterator()
                .asSequence()
                .filter { it.isItemOfType(item) }
                .firstOrNull()
                ?: throw NoSuchElementException("Can't determine a type of item at position $position: $item.")
        return itemData.itemType
    }

    @Contract(pure = true)
    override fun getItemId(position: Int): Long {
        return when {
            itemIdMapper != null -> itemIdMapper.invoke(position, get(position))
            else -> RecyclerView.NO_ID
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // TODO: implement state saving
        if (selectionTracker is StateAware) {
            selectionTracker.onSaveInstanceState(outState)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        // TODO: implement state restoration
        if (selectionTracker is StateAware) {
            selectionTracker.onRestoreInstanceState(savedInstanceState)
        }
    }

    /**
     * Get item by its position in adapter.
     *
     * @param position position of item in adapter
     *
     * @return item at given position or `null` if page with this item is not loaded yet
     *
     * @throws IndexOutOfBoundsException if `position < 0 || position >= getItemCount()`
     */
    @Contract(pure = true)
    @Throws(IndexOutOfBoundsException::class)
    operator fun get(position: Int) = getItem(position)

    /**
     * Get position of item in adapter.
     *
     * @param item item that might be in adapter
     *
     * @return position of item or [RecyclerView.NO_POSITION]
     */
    @Contract(pure = true)
    fun indexOf(item: T): Int {
        val list = currentList ?: return RecyclerView.NO_POSITION
        return list.indexOf(item) + list.positionOffset
    }

    /**
     * Check if adapter contains an item.
     *
     * @param item item that might be in adapter
     *
     * @return `true` if adapter contains item, `false` otherwise
     */
    @Contract(pure = true)
    fun contains(item: T): Boolean = indexOf(item) > RecyclerView.NO_POSITION

    /**
     * Start observing `LiveData` using provided `LifecycleOwner`. Observing will automatically stopped with [Lifecycle.Event.ON_DESTROY] event.
     *
     * Adapter will be populated when `LifecycleOwner` receives [Lifecycle.Event.ON_START].
     *
     * @param owner instance of [LifecycleOwner]
     */
    fun observe(owner: LifecycleOwner) {
        liveData.observe(owner, liveDataObserver)
    }

    /**
     * Filter adapter with given query. It's up to [ConfigLiveData.Factory] to perform all filtering though. When it receive [Configuration]
     * the passed `query` can be accessed via [CONFIG_KEY_FILTER] key.
     *
     * The contract of this method: pass any value to start filtering, pass `null` to clear the filter.
     *
     * @param query any filter query
     *
     * @see isFiltered
     * @see getFilterQuery
     */
    fun filter(query: Any?) {
        configuration[CONFIG_KEY_FILTER] = query
    }

    /**
     * Get query used to filter an adapter.
     *
     * @return query used to filter an adapter or `null`
     *
     * @see filter
     * @see isFiltered
     */
    fun <T> getFilterQuery(): T? = configuration[CONFIG_KEY_FILTER]

    /**
     * Cast adapter to specific type.
     *
     * @param T cast type
     */
    fun <T> toTyped(): PagedAdapter<T> {
        @Suppress("UNCHECKED_CAST")
        return this as PagedAdapter<T>
    }

    /**
     * Builder stage that allows to set a live data.
     *
     * @param T type of items
     */
    interface StageDataSource<T> {

        /**
         * Set a live data that will populate adapter.
         *
         * @param liveData instance of `ConfigLiveData`
         *
         * @return same builder for chaining calls
         */
        fun withLiveData(liveData: ConfigLiveData<PagedList<T>?>): StageOptional<T>
    }

    /**
     * Final stage of building a [PagedAdapter].
     */
    interface StageOptional<T> {

        /**
         * Set callback for calculating the diff between two non-null items in a list.
         *
         * @param value instance of callback
         *
         * @return same builder for chaining calls
         */
        fun withDiffItemCallback(value: DiffUtil.ItemCallback<T>): StageOptional<T>

        /**
         * Set specific layout for placeholders. Same as calling [withPlaceholderType(Int, ViewHolderFactory)][withPlaceholderType]
         * with [ViewHolder.Factory] as `ViewHolderFactory`. If root view implements [ViewHolder] or [PlaceholderViewHolder],
         * no appropriate [ViewHolder.bind] methods will be called.
         *
         * @param layoutId resource ID of layout
         *
         * @return same builder for chaining calls
         *
         * @see PlaceholderViewHolder
         */
        fun withSimplePlaceholderType(@LayoutRes layoutId: Int): StageOptional<T>

        /**
         * Set specific layout for placeholders. Root view of layout must implement [PlaceholderViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         *
         * @return same builder for chaining calls
         */
        fun withPlaceholderType(@LayoutRes layoutId: Int): StageOptional<T>

        /**
         * Set specific layout for placeholders. View with specified ID must implement [PlaceholderViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param viewId view ID of [PlaceholderViewHolder]
         *
         * @return same builder for chaining calls
         */
        fun withPlaceholderType(@LayoutRes layoutId: Int, @IdRes viewId: Int): StageOptional<T>

        /**
         * Set specific layout for placeholders.
         *
         * @param layoutId resource ID of layout
         * @param viewHolderFactory `ViewHolder`'s factory that creates instances of [PlaceholderViewHolder].
         *
         * @return same builder for chaining calls
         */
        fun withPlaceholderType(@LayoutRes layoutId: Int, viewHolderFactory: ViewHolderFactory): StageOptional<T>

        /**
         * Set specific layout for placeholders.
         *
         * @param layoutId resource ID of layout
         * @param viewHolderFactory `ViewHolder`'s factory that creates instances of [PlaceholderViewHolder].
         *
         * @return same builder for chaining calls
         */
        fun withPlaceholderType(@LayoutRes layoutId: Int, viewHolderFactory: VhFactory): StageOptional<T>

        /**
         * Set layout for specific item. Root view of layout must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param itemTypePredicate predicate used to determine if item belongs to this type
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int, itemTypePredicate: Predicate<Any>): StageOptional<T>

        /**
         * Set layout for specific item. Root view of layout must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param clazz class of item that will be used to determine if item belongs to this type
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int, clazz: Class<*>): StageOptional<T>

        /**
         * Set layout for specific item. View with specified ID must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param itemTypePredicate predicate used to determine if item belongs to this type
         * @param viewId view ID of [ViewHolder]
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int,
                         itemTypePredicate: Predicate<Any>,
                         @IdRes viewId: Int): StageOptional<T>

        /**
         * Set layout for specific item. View with specified ID must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param clazz class of item that will be used to determine if item belongs to this type
         * @param viewId view ID of [ViewHolder]
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int,
                         clazz: Class<*>,
                         @IdRes viewId: Int): StageOptional<T>

        /**
         * Set layout for specific item. View with specified ID must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param itemTypePredicate predicate used to determine if item belongs to this type
         * @param viewHolderFactory custom provider of [ViewHolder]
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int,
                         itemTypePredicate: Predicate<Any>,
                         viewHolderFactory: ViewHolderFactory): StageOptional<T>

        /**
         * Set layout for specific item. View with specified ID must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param clazz class of item that will be used to determine if item belongs to this type
         * @param viewHolderFactory custom provider of [ViewHolder]
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int,
                         clazz: Class<*>,
                         viewHolderFactory: ViewHolderFactory): StageOptional<T>

        /**
         * Set layout for specific item. View with specified ID must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param itemTypePredicate predicate used to determine if item belongs to this type
         * @param viewHolderFactory custom provider of [ViewHolder]
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int,
                         itemTypePredicate: Predicate<Any>,
                         viewHolderFactory: VhFactory): StageOptional<T>


        /**
         * Set layout for specific item. Root view of layout must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param clazz class of item that will be used to determine if item belongs to this type
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int, clazz: KClass<*>): StageOptional<T>

        /**
         * Set layout for specific item. View with specified ID must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param clazz class of item that will be used to determine if item belongs to this type
         * @param viewId view ID of [ViewHolder]
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int,
                         clazz: KClass<*>,
                         @IdRes viewId: Int): StageOptional<T>

        /**
         * Set layout for specific item. View with specified ID must implement [ViewHolder] interface.
         *
         * @param layoutId resource ID of layout
         * @param clazz class of item that will be used to determine if item belongs to this type
         * @param viewHolderFactory custom provider of [ViewHolder]
         *
         * @return same builder for chaining calls
         */
        fun withItemType(@LayoutRes layoutId: Int,
                         clazz: KClass<*>,
                         viewHolderFactory: VhFactory): StageOptional<T>


        /**
         * Set mapper that returns the stable ID for the item at position.
         *
         * @param mapper instance of mapper
         *
         * @return same builder for chaining calls
         *
         * @see RecyclerView.Adapter.hasStableIds
         * @see RecyclerView.Adapter.getItemId
         */
        fun withItemIdMapper(mapper: ItemIdMapper<T>): StageOptional<T>

        /**
         * Set mapper that returns the stable ID for the item at position.
         *
         * @param mapper instance of mapper
         *
         * @return same builder for chaining calls
         *
         * @see RecyclerView.Adapter.hasStableIds
         * @see RecyclerView.Adapter.getItemId
         */
        fun withItemIdMapper(mapper: IdMapper<T>): StageOptional<T>

        /**
         * Set listener that will be called when user clicks on item.
         *
         * @param onItemClickListener instance of listener
         *
         * @return same builder for chaining calls
         */
        fun withOnItemClickListener(onItemClickListener: OnItemClickListener<T>): StageOptional<T>

        /**
         * Set listener that will be called when user clicks on item.
         *
         * @param onItemClickListener instance of listener
         *
         * @return same builder for chaining calls
         */
        fun withOnItemClickListener(onItemClickListener: OnClickListener<T>): StageOptional<T>

        /**
         * Set listener that will be called when user long clicks on item.
         *
         * @param onItemLongClickListener instance of listener
         *
         * @return same builder for chaining calls
         */
        fun withOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener<T>): StageOptional<T>

        /**
         * Set listener that will be called when user long clicks on item.
         *
         * @param onItemLongClickListener instance of listener
         *
         * @return same builder for chaining calls
         */
        fun withOnItemLongClickListener(onItemLongClickListener: OnLongClickListener<T>): StageOptional<T>

        /**
         * Set marker that will be passed to [ViewHolder.bind] method when data source invalidated.
         *
         * @param marker any marker
         *
         * @return same builder for chaining calls
         */
        fun withOnLiveDataInvalidatedMarker(marker: Any): StageOptional<T>

        /**
         * Set selection tracker used to select items in adapter.
         *
         * @param builder instance of selection tracker builder
         *
         * @return same builder for chaining calls
         */
        fun withSelectionTracker(builder: SelectionTracker.StageObservers<T>): StageOptional<T>

        /**
         * Create an instance of adapter with provided configuration.
         *
         * @return an instance of adapter
         */
        fun build(): PagedAdapter<T>
    }

    /**
     * Builder for [PagedAdapter].
     *
     * @constructor Create a new instance of builder.
     */
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    private class Builder<T> :
            StageDataSource<T>,
            StageOptional<T> {

        private lateinit var liveData: ConfigLiveData<PagedList<T>?>
        private var diffCallback: DiffUtil.ItemCallback<T>? = null
        private var invalidationMarker: Any? = null
        private var itemIdMapper: IdMapper<T>? = null
        private var onItemClickListener: OnClickListener<T>? = null
        private var onItemLongClickListener: OnLongClickListener<T>? = null
        private var selectionTrackerBuilder: SelectionTracker.StageObservers<T>? = null
        private val itemDataMap = SparseArray<ItemData>()
        private val nextItemType = AtomicInteger(1)

        override fun withLiveData(liveData: ConfigLiveData<PagedList<T>?>): StageOptional<T> {
            this.liveData = liveData
            return this
        }

        override fun withDiffItemCallback(value: DiffUtil.ItemCallback<T>): StageOptional<T> {
            diffCallback = value
            return this
        }

        override fun withSimplePlaceholderType(layoutId: Int): StageOptional<T> {
            return withPlaceholderType(layoutId, ViewHolder.Factory)
        }

        override fun withPlaceholderType(@LayoutRes layoutId: Int): StageOptional<T> {
            return withPlaceholderType(layoutId) { tryWrap(it) }
        }

        override fun withPlaceholderType(@LayoutRes layoutId: Int, @IdRes viewId: Int): StageOptional<T> {
            return withPlaceholderType(layoutId) { tryWrap(it, viewId) }
        }

        override fun withPlaceholderType(@LayoutRes layoutId: Int, viewHolderFactory: ViewHolderFactory): StageOptional<T> {
            return withItemType(TYPE_PLACEHOLDER, layoutId, { it as Any? == null }, viewHolderFactory::create)
        }

        override fun withItemType(@LayoutRes layoutId: Int, itemTypePredicate: Predicate<Any>): StageOptional<T> {
            return withItemType(nextItemType.getAndIncrement(), layoutId, itemTypePredicate) { tryWrap(it) }
        }

        override fun withItemType(@LayoutRes layoutId: Int, clazz: Class<*>): StageOptional<T> {
            return withItemType(nextItemType.getAndIncrement(), layoutId, clazz::isInstance) { tryWrap(it) }
        }

        override fun withItemType(@LayoutRes layoutId: Int,
                                  itemTypePredicate: Predicate<Any>,
                                  @IdRes viewId: Int): StageOptional<T> {
            return withItemType(nextItemType.getAndIncrement(), layoutId, itemTypePredicate) { tryWrap(it, viewId) }
        }

        override fun withItemType(@LayoutRes layoutId: Int,
                                  clazz: Class<*>, @IdRes viewId: Int): StageOptional<T> {
            return withItemType(nextItemType.getAndIncrement(), layoutId, clazz::isInstance) { tryWrap(it, viewId) }
        }

        override fun withItemType(@LayoutRes layoutId: Int,
                                  clazz: Class<*>,
                                  viewHolderFactory: ViewHolderFactory): StageOptional<T> {
            return withItemType(layoutId, clazz::isInstance, viewHolderFactory)
        }

        override fun withItemType(@LayoutRes layoutId: Int,
                                  itemTypePredicate: Predicate<Any>,
                                  viewHolderFactory: ViewHolderFactory): StageOptional<T> {
            return withItemType(nextItemType.getAndIncrement(), layoutId, itemTypePredicate, viewHolderFactory::create)
        }

        override fun withPlaceholderType(@LayoutRes layoutId: Int, viewHolderFactory: VhFactory): StageOptional<T> =
                withItemType(TYPE_PLACEHOLDER, layoutId, { it as Any? == null }, viewHolderFactory)

        override fun withItemType(@LayoutRes layoutId: Int, clazz: KClass<*>): StageOptional<T> =
                withItemType(layoutId, clazz::isInstance)


        override fun withItemType(@LayoutRes layoutId: Int,
                                  clazz: KClass<*>,
                                  @IdRes viewId: Int): StageOptional<T> =
                withItemType(layoutId, clazz::isInstance, viewId)

        override fun withItemType(@LayoutRes layoutId: Int,
                                  clazz: KClass<*>,
                                  viewHolderFactory: VhFactory): StageOptional<T> =
                withItemType(nextItemType.getAndIncrement(), layoutId, clazz::isInstance, viewHolderFactory)

        override fun withItemType(@LayoutRes layoutId: Int,
                                  itemTypePredicate: Predicate<Any>,
                                  viewHolderFactory: VhFactory): StageOptional<T> {
            return withItemType(nextItemType.getAndIncrement(), layoutId, itemTypePredicate, viewHolderFactory)
        }

        private fun withItemType(type: Int,
                                 @LayoutRes layoutId: Int,
                                 itemTypePredicate: Predicate<Any>,
                                 viewHolderFactory: VhFactory): StageOptional<T> {
            if (itemDataMap[type] != null) {
                throw IllegalArgumentException("Item type $type already registered.")
            }
            itemDataMap[type] = ItemData(type, layoutId, itemTypePredicate, viewHolderFactory)
            return this
        }

        override fun withItemIdMapper(mapper: ItemIdMapper<T>): StageOptional<T> = withItemIdMapper(mapper::getItemId)

        override fun withOnItemClickListener(onItemClickListener: OnItemClickListener<T>): StageOptional<T> =
                withOnItemClickListener(onItemClickListener::onItemClick)

        override fun withOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener<T>): StageOptional<T> =
                withOnItemLongClickListener(onItemLongClickListener::onItemLongClick)

        override fun withOnLiveDataInvalidatedMarker(marker: Any): StageOptional<T> {
            this.invalidationMarker = marker
            return this
        }

        override fun withSelectionTracker(builder: SelectionTracker.StageObservers<T>): StageOptional<T> {
            this.selectionTrackerBuilder = builder
            return this
        }

        override fun withOnItemClickListener(
                onItemClickListener: OnClickListener<T>): StageOptional<T> {
            this.onItemClickListener = onItemClickListener
            return this
        }

        override fun withOnItemLongClickListener(
                onItemLongClickListener: OnLongClickListener<T>): StageOptional<T> {
            this.onItemLongClickListener = onItemLongClickListener
            return this
        }

        override fun withItemIdMapper(mapper: IdMapper<T>): StageOptional<T> {
            this.itemIdMapper = mapper
            return this
        }

        override fun build(): PagedAdapter<T> {
            // configuring completed, now all changes must notify observers
            this.liveData.configuration.notifyConfigurationChanges = true
            // copy all passed argument (if possible)
            @Suppress("UNCHECKED_CAST")
            val diffCallback = this.diffCallback ?: SimpleDiffCallback as DiffUtil.ItemCallback<T>
            val itemDataMap = this.itemDataMap.clone()
            return PagedAdapter(
                    this.liveData,
                    itemDataMap,
                    this.itemIdMapper,
                    this.onItemClickListener,
                    this.onItemLongClickListener,
                    this.invalidationMarker,
                    this.selectionTrackerBuilder,
                    diffCallback
            )
        }

        private fun tryWrap(rootView: View, @IdRes viewId: Int = 0): RecyclerView.ViewHolder {
            val v = if (viewId == 0) {
                rootView
            } else {
                rootView.findViewById(viewId)
                        ?: throw NoSuchElementException("Can't find view with id: ${getId(rootView.context, viewId)}.")
            }
            return when (v) {
                is ViewHolder<*> -> ViewHolderWrapper(v.cast(), rootView)
                else -> failViewNotImplementsViewHolder(v)
            }
        }

        private fun getId(context: Context, @IdRes id: Int): String {
            return if (id == View.NO_ID) {
                "no-id"
            } else {
                try {
                    context.resources.getResourceName(id)
                } catch (e: Resources.NotFoundException) {
                    "$id"
                }
            }
        }

        private fun failViewNotImplementsViewHolder(view: View): Nothing {
            val message = "View ${view::class.qualifiedName} must implement ${ViewHolder::class.qualifiedName} or you must set view holder " +
                    "provider explicitly."
            throw IllegalArgumentException(message)
        }
    }

    /**
     * Listener that will be called when user clicks on item.
     *
     * @param T type of items
     */
    @FunctionalInterface
    interface OnItemClickListener<T> {

        /**
         * Called when user clicks on item.
         *
         * @param adapter parent adapter that holds this listener
         * @param view view that user clicked on
         * @param position position of item in adapter
         * @param itemId ID of item
         */
        fun onItemClick(adapter: PagedAdapter<T>, view: View, position: Int, itemId: Long)
    }

    /**
     * Factory that creates a new `ViewHolder` for a view.
     */
    @FunctionalInterface
    interface ViewHolderFactory {

        /**
         * Create a new `ViewHolder` for a [view].
         *
         * @param view instance of `View`
         *
         * @return a new instance of `ViewHolder`
         */
        fun create(view: View): RecyclerView.ViewHolder
    }

    /**
     * Listener that will be called when user long clicks on item.
     *
     * @param T type of items
     */
    @FunctionalInterface
    interface OnItemLongClickListener<T> {

        /**
         * Called when user long clicks on item.
         *
         * @param adapter parent adapter that holds this listener
         * @param view view that user clicked on
         * @param position position of item in adapter
         * @param itemId ID of item
         *
         * @return return `true` if click event was consumed, `false` otherwise
         */
        fun onItemLongClick(adapter: PagedAdapter<T>, view: View, position: Int, itemId: Long): Boolean
    }

    /**
     * Mapper that returns the stable ID for the item at position.
     *
     * @param T type of items
     *
     * @see RecyclerView.Adapter.getItemId
     * @see RecyclerView.Adapter.hasStableIds
     */
    @FunctionalInterface
    interface ItemIdMapper<T> {

        /**
         * Get ID of item.
         *
         * @param position position of item in adapter
         * @param item item at specified position
         *
         * @return stable ID of item
         */
        fun getItemId(position: Int, item: T?): Long
    }
}

private class ViewHolderWrapper(viewHolder: ViewHolder<Any>, rootView: View) : RecyclerView.ViewHolder(rootView), ViewHolder<Any> by viewHolder

/**
 * View holder provider. It determines what layout to inflate, inflates it and passes to wrapped provider.
 */
private data class ItemData(
        val itemType: Int,
        @LayoutRes private val layoutId: Int,
        private val itemTypePredicate: Predicate<Any>,
        private val host: VhFactory
) {

    /**
     * Provides a new view holder.
     *
     * @param inflater instance of `LayoutInflater`
     * @param parent parent view
     */
    fun provideViewHolder(inflater: LayoutInflater, parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = inflater.inflate(layoutId, parent, false)
        return host(view)
    }

    fun isItemOfType(item: Any): Boolean = itemTypePredicate(item)
}

/**
 * Simple [DiffUtil.ItemCallback] that just checks items for equality.
 */
private object SimpleDiffCallback : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
}