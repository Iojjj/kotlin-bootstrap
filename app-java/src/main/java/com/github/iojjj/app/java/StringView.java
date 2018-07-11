package com.github.iojjj.app.java;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.github.iojjj.app.core.BaseStringView;
import com.github.iojjj.bootstrap.adapters.adapter.AdapterUtils;
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter;
import com.github.iojjj.bootstrap.adapters.viewholder.ViewHolder;

import java.util.List;

public final class StringView extends BaseStringView implements ViewHolder<String> {

    public StringView(@NonNull final Context context) {
        super(context, "Java");
    }

    public StringView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs, "Java");
    }

    public StringView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr, "Java");
    }

    @Override
    public void onCreated(@NonNull final PagedAdapter<?> adapter) {
        /* no-op */
    }

    @Override
    public void bind(@NonNull final PagedAdapter<?> adapter, final int position, final String item) {
        if (adapter.isFiltered()) {
            updateText(AdapterUtils.highlightQuery(item, adapter.getFilterQuery(), Color.RED));
        } else {
            updateText(item);
        }
        setActivated(adapter.<String>toTyped().getSelectionTracker().getSelection().contains(item));
    }

    @Override
    public void bind(@NonNull final PagedAdapter<?> adapter, final int position, final String item, final List<Object> payload) {
        bind(adapter, position, item);
    }
}
