package com.github.iojjj.app.core;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

public abstract class BaseStringView extends AppCompatTextView {

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

    public void updateText(@Nullable final CharSequence text) {
        setText(TextUtils.concat(mTitle, ": ", text));
    }
}
