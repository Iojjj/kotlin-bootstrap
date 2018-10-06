package com.github.iojjj.app.core;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.concurrent.TimeUnit;

public abstract class BaseRecyclerViewActivity extends AppCompatActivity {

    public static final String MARKER_DATA_SOURCE = "DATA_SOURCE_INVALIDATED";
    public static final String KEY_REMOVED = "removed_list";
    public static final int DATA_SET_SIZE = 5_000;
    public static final int PAGE_SIZE = 50;

    @NonNull
    private final Subject<String> mQueryProducer = PublishSubject.create();
    @NonNull
    private final SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String newText) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mQueryProducer.onNext(newText);
            return true;
        }
    };
    @Nullable
    private ActionMode mActionMode;
    @Nullable
    private Disposable mFilterOperation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        initToolbar(R.id.toolbar);
        initSearch();
        final RecyclerView list = findViewById(R.id.list);
        initList(list);
        initFastScroll(list);
    }

    private void initSearch() {
        mFilterOperation = mQueryProducer
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(newText -> filter(TextUtils.isEmpty(newText) ? null : newText));
    }

    protected void initList(@NonNull final RecyclerView list) {
        list.setLayoutManager(new LinearLayoutManager(this));
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        list.setHasFixedSize(true);
    }

    private void initFastScroll(@NonNull final RecyclerView list) {
        final FastScroller fastScroll = findViewById(R.id.fastscroll);
        fastScroll.setRecyclerView(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(mQueryTextListener);
        return true;
    }

    @Override
    protected void onDestroy() {
        if (mFilterOperation != null) {
            mFilterOperation.dispose();
            mFilterOperation = null;
        }
        super.onDestroy();
    }

    protected void setActionModeTitle(@Nullable final CharSequence title) {
        if (mActionMode != null) {
            mActionMode.setTitle(title);
        }
    }

    protected void startSelection() {
        mActionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_action_mode, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_remove) {
                    onActionModeItemDeleteClicked();
                    stopSelection();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                mActionMode = null;
                onActionModeDestroyed();
            }
        });
    }

    protected boolean isSelectionStarted() {
        return mActionMode != null;
    }

    protected void stopSelection() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    protected abstract void initToolbar(@IdRes int layoutId);

    protected abstract void filter(@Nullable String query);

    protected abstract void onActionModeItemDeleteClicked();

    protected abstract void onActionModeDestroyed();
}
