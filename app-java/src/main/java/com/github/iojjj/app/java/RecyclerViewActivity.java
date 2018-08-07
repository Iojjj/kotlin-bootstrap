package com.github.iojjj.app.java;

import android.annotation.SuppressLint;
import android.arch.core.executor.ArchTaskExecutor;
import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.PagedList;
import android.arch.paging.PositionalDataSource;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.Toast;
import androidx.app.AppCompatActivityKt;
import com.github.iojjj.app.core.BaseRecyclerViewActivity;
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter;
import com.github.iojjj.bootstrap.adapters.data.ConfigLiveData;
import com.github.iojjj.bootstrap.adapters.data.Configuration;
import com.github.iojjj.bootstrap.adapters.selection.selections.MutableSelection;
import com.github.iojjj.bootstrap.adapters.selection.selections.Selection;
import com.github.iojjj.bootstrap.adapters.selection.trackers.SelectionTracker;
import com.github.iojjj.bootstrap.utils.BackPressHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class RecyclerViewActivity extends BaseRecyclerViewActivity {

    private PagedAdapter<String> mAdapter;
    private Toast mToast;
    private final BackPressHandler mBackPressHandler = BackPressHandler.newInstance(2, TimeUnit.SECONDS, this::showOnBackPressPrompt);

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensureAdapter().onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void initToolbar(@IdRes final int layoutId) {
        AppCompatActivityKt.setSupportActionBar(this, layoutId);
    }

    @Override
    protected void initList(@NonNull RecyclerView list) {
        super.initList(list);
//        list.addItemDecoration(PaddingDecoration.newBuilder()
//                .withStartPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding_start))
//                .withEndPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding_end))
//                .build());
        final PagedAdapter<String> adapter = ensureAdapter();
        list.setAdapter(adapter);
        adapter.observe(this);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    private PagedAdapter<String> ensureAdapter() {
        if (mAdapter == null) {

            final MutableLiveData<List<String>> data = new MutableLiveData<>();
//            final ConfigLiveData<List<String>> testData = ConfigLiveData.<List<String>>ofSingle()
//                    .withLiveData(data)
//                    .withFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
//                    .build();
//            testData.observe(this, strings -> Log.d("TEST_DATA", strings == null ? "null" : Arrays.toString(strings.toArray())));

            Observable.interval(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(count -> data.postValue(generateCollection(count)));

            final ConfigLiveData<PagedList<String>> liveData = ConfigLiveData.<Integer, String>ofPagedList()
                    .withLiveData(data, strings -> {
                        final List<String> removed = mAdapter.getConfiguration().get(KEY_REMOVED);
                        final String filter = mAdapter.getConfiguration().getOrDefault(PagedAdapter.CONFIG_KEY_FILTER, (String) null);
                        if (filter == null && (removed == null || removed.isEmpty())) {
                            return strings;
                        }
                        final List<String> filteredData = new ArrayList<>();
                        for (final String d : strings) {
                            final boolean acceptedByFilter = filter == null || d.toLowerCase().contains(filter.toLowerCase());
                            final boolean acceptedByRemoved = removed == null || !removed.contains(d);
                            if (acceptedByFilter && acceptedByRemoved) {
                                filteredData.add(d);
                            }
                        }
                        return filteredData;
                    })
                    .withPageSize(PAGE_SIZE)
                    .withFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                    .withObserveInstantly(true)
                    .build();
            mAdapter = PagedAdapter.newBuilderWith(liveData)
                    .withSimplePlaceholderType(R.layout.list_item_placeholder)
                    .withItemType(R.layout.list_item_string, String.class)
                    // when configuration changes (by any reason) this marker will be sent to ViewHolder's bind methods
                    // this is useful when you want to highlight a search query
                    .withOnLiveDataInvalidatedMarker(MARKER_DATA_SOURCE)
                    .withOnItemClickListener((adapter, view, position, itemId) -> {
                        if (isSelectionStarted()) {
                            adapter.getSelectionTracker().toggle(adapter.get(position));
                        }
                    })
                    .withOnItemLongClickListener((PagedAdapter.OnItemLongClickListener<String>) (adapter, view, position, itemId) -> {
                        final boolean shouldSelect = !isSelectionStarted();
                        if (shouldSelect) {
                            adapter.getSelectionTracker().toggle(adapter.get(position));
                        }
                        return shouldSelect;
                    })
                    .withSelectionTracker(SelectionTracker.Factory.<String>newBuilder()
                            .withMultipleSelection()
                            .withSelectionPredicate((SelectionTracker.SelectionPredicate<String>) item -> !"Item 1".equals(item))
                            .addObserver(new SelectionTracker.SelectionAdapter<String>() {
                                @Override
                                public void onSelectionStarted(@NonNull final Selection<String> selection) {
                                    startSelection();
                                }

                                @Override
                                public void onSelectionChanged(@NotNull final Selection<String> selection) {
                                    setActionModeTitle(String.valueOf(selection.getSize()));
                                }

                                @Override
                                public void onSelectionStopped(@NonNull final Selection<String> selection) {
                                    stopSelection();
                                }
                            })
                    )
                    .build();
        }
        return mAdapter;
    }

    private List<String> generateCollection(final long count) {
        final List<String> data = new ArrayList<>();
        final int size = (int) (count % 10);
        for (int i = 0; i < size; i++) {
            data.add(String.format(Locale.US, "Item %d", i + 1));
        }
        return data;
    }

    @Override
    protected void filter(@Nullable String query) {
        ensureAdapter().filter(query);
    }

    @Override
    protected void onActionModeItemDeleteClicked() {
        final PagedAdapter<String> adapter = ensureAdapter();
        final List<String> removed = adapter.getConfiguration().getOrDefault(KEY_REMOVED, ArrayList::new);
        final MutableSelection<String> snapshot = adapter.getSelectionTracker().getSelection().snapshot();
        for (final String item : snapshot) {
            removed.add(item);
        }
        adapter.getConfiguration().notifyConfigurationChanged();
    }

    @Override
    protected void onActionModeDestroyed() {
        ensureAdapter().getSelectionTracker().clear();
    }

    @Override
    public void onBackPressed() {
        if (!mBackPressHandler.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void showOnBackPressPrompt() {
        final SpannableStringBuilder builder = new SpannableStringBuilder("Press Back again to close the app.");
        final int start = builder.toString().indexOf("Back");
        final int end = start + "Back".length();
        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, builder, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private static final class StringDataSource extends PositionalDataSource<String> {

        @NonNull
        private final List<String> mData;

        private StringDataSource(@NonNull final List<String> data) {
            mData = data;
        }

        @Override
        public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<String> callback) {
            final int size = mData.size();
            final int startPosition = computeInitialLoadPosition(params, size);
            final int loadSize = computeInitialLoadSize(params, startPosition, size);
            callback.onResult(mData.subList(startPosition, startPosition + loadSize), startPosition, size);
        }

        @Override
        public void loadRange(@NonNull final LoadRangeParams params, @NonNull final LoadRangeCallback<String> callback) {
            callback.onResult(mData.subList(params.startPosition, params.startPosition + params.loadSize));
        }

        private static final class Factory implements ConfigLiveData.Factory<DataSource<Integer, String>> {

            private List<String> mData;

            @Override
            public DataSource<Integer, String> create(@NonNull final Configuration configuration) {
                final List<String> removed = configuration.get(KEY_REMOVED);
                final String filter = configuration.getOrDefault(PagedAdapter.CONFIG_KEY_FILTER, (String) null);
                if (filter == null && (removed == null || removed.isEmpty())) {
                    return new StringDataSource(ensureData());
                }
                final List<String> data = ensureData();
                final List<String> filteredData = new ArrayList<>();
                for (final String d : data) {
                    final boolean acceptedByFilter = filter == null || d.toLowerCase().contains(filter.toLowerCase());
                    final boolean acceptedByRemoved = removed == null || !removed.contains(d);
                    if (acceptedByFilter && acceptedByRemoved) {
                        filteredData.add(d);
                    }
                }
                return new StringDataSource(filteredData);
            }

            private List<String> ensureData() {
                if (mData == null) {
                    mData = new ArrayList<>();
                    for (int i = 0; i < DATA_SET_SIZE; i++) {
                        mData.add(String.format(Locale.US, "Item %d", i + 1));
                    }
                }
                return mData;
            }
        }
    }
}
