package com.github.iojjj.app.java;

import android.arch.paging.DataSource;
import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter;
import com.github.iojjj.bootstrap.adapters.data.ConfigLiveData;
import com.github.iojjj.bootstrap.adapters.data.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.iojjj.app.core.BaseRecyclerViewActivity.DATA_SET_SIZE;
import static com.github.iojjj.app.core.BaseRecyclerViewActivity.KEY_REMOVED;

final class StringDataSource extends PositionalDataSource<String> {

    @NonNull
    private final List<String> mData;

    private StringDataSource(@NonNull final List<String> data) {
        mData = data;
    }

    static List<String> filter(@NonNull final List<String> strings, @NonNull final Configuration configuration) {
        final List<String> removed = configuration.get(KEY_REMOVED);
        final String filter = configuration.getOrDefault(PagedAdapter.CONFIG_KEY_FILTER, (String) null);
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

    static final class DataFactory implements ConfigLiveData.DataFactory<DataSource<Integer, String>> {

        private List<String> mData;

        @Override
        public DataSource<Integer, String> create(@NonNull final Configuration configuration) {
            return new StringDataSource(filter(ensureData(), configuration));
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
