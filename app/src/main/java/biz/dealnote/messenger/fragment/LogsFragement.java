package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.LogsAdapter;
import biz.dealnote.messenger.adapter.horizontal.HorizontalOptionsAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.LogEvent;
import biz.dealnote.messenger.model.LogEventType;
import biz.dealnote.messenger.model.LogEventWrapper;
import biz.dealnote.messenger.mvp.presenter.LogsPresenter;
import biz.dealnote.messenger.mvp.view.ILogsView;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class LogsFragement extends BasePresenterFragment<LogsPresenter, ILogsView>
        implements ILogsView, HorizontalOptionsAdapter.Listener<LogEventType>, LogsAdapter.ActionListener {

    public static LogsFragement newInstance() {
        Bundle args = new Bundle();
        LogsFragement fragment = new LogsFragement();
        fragment.setArguments(args);
        return fragment;
    }

    private HorizontalOptionsAdapter<LogEventType> mTypesAdapter;

    private LogsAdapter mLogsAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView mEmptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_logs, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        View headerView = inflater.inflate(R.layout.header_logs, recyclerView, false);

        RecyclerView typesRecyclerView = (RecyclerView) headerView.findViewById(R.id.types_recycler_view);
        typesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        mTypesAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
        mTypesAdapter.setListener(this);
        typesRecyclerView.setAdapter(mTypesAdapter);

        mLogsAdapter = new LogsAdapter(Collections.emptyList(), this);
        mLogsAdapter.addHeader(headerView);

        recyclerView.setAdapter(mLogsAdapter);

        mEmptyText = (TextView) root.findViewById(R.id.empty_text);
        return root;
    }

    @Override
    public void displayTypes(List<LogEventType> types) {
        if(nonNull(mTypesAdapter)){
            mTypesAdapter.setItems(types);
        }
    }

    @Override
    public void displayData(List<LogEventWrapper> events) {
        if(nonNull(mLogsAdapter)){
            mLogsAdapter.setItems(events);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(nonNull(actionBar)){
            actionBar.setTitle(R.string.application_logs);
            actionBar.setSubtitle(null);
        }

        if (getActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) getActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    public void notifyEventDataChanged() {
        if(nonNull(mLogsAdapter)){
            mLogsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyTypesDataChanged() {
        if(nonNull(mTypesAdapter)){
            mTypesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if(nonNull(mEmptyText)){
            mEmptyText.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public IPresenterFactory<LogsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LogsPresenter(saveInstanceState);
    }

    @Override
    protected String tag() {
        return LogsFragement.class.getSimpleName();
    }

    @Override
    public void onOptionClick(LogEventType entry) {
        getPresenter().fireTypeClick(entry);
    }

    @Override
    public void onShareClick(LogEventWrapper wrapper) {
        LogEvent event = wrapper.getEvent();
        Utils.shareLink(getActivity(), event.getBody(), event.getTag());
    }
}
