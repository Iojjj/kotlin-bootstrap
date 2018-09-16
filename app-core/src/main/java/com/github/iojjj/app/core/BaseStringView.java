package com.github.iojjj.app.core;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.github.iojjj.bootstrap.adapters.adapter.PagedAdapter;
import com.github.iojjj.bootstrap.adapters.viewholder.ViewHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BaseStringView extends AppCompatTextView implements ViewHolder<String> {

    @NonNull
    private final String mTitle;

    public BaseStringView(@NonNull final Context context, @NonNull final String title) {
        super(context);
        mTitle = title;
    }

    public BaseStringView(@NonNull final Context context, @Nullable final AttributeSet attrs, @NonNull final String title) {
        super(context, attrs);
        mTitle = title;
    }

    public BaseStringView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr,
                          @NonNull final String title) {
        super(context, attrs, defStyleAttr);
        mTitle = title;
    }

    @Override
    public void onCreated(@NotNull PagedAdapter<?> adapter) {
        /* no-op */
    }

    public void updateText(@Nullable final CharSequence text) {
        setText(TextUtils.concat(mTitle, ": ", text));
    }

    @Override
    public void bind(@NonNull final PagedAdapter<?> adapter, final int position, final String item, final List<Object> payload) {
        bind(adapter, position, item);
    }
}
