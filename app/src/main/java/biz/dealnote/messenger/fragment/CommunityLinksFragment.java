package biz.dealnote.messenger.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.CommunityLinksAdapter;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.mvp.presenter.CommunityLinksPresenter;
import biz.dealnote.messenger.mvp.view.ICommunityLinksView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityLinksFragment extends BasePresenterFragment<CommunityLinksPresenter, ICommunityLinksView>
        implements ICommunityLinksView, CommunityLinksAdapter.ActionListener {

    private CommunityLinksAdapter mLinksAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static CommunityLinksFragment newInstance(int accountId, int groupId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        CommunityLinksFragment fragment = new CommunityLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_links, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mLinksAdapter = new CommunityLinksAdapter(Collections.emptyList());
        mLinksAdapter.setActionListener(this);

        recyclerView.setAdapter(mLinksAdapter);

        root.findViewById(R.id.button_add).setOnClickListener(v -> getPresenter().fireButtonAddClick());
        return root;
    }

    @Override
    public IPresenterFactory<CommunityLinksPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunityLinksPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.GROUP_ID),
                saveInstanceState
        );
    }

    @Override
    protected String tag() {
        return CommunityLinksFragment.class.getSimpleName();
    }

    @Override
    public void displayRefreshing(boolean loadingNow) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(loadingNow);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(nonNull(mLinksAdapter)){
            mLinksAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayData(List<VKApiCommunity.Link> links) {
        if(nonNull(mLinksAdapter)){
            mLinksAdapter.setData(links);
        }
    }

    @Override
    public void openLink(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(intent);
    }

    @Override
    public void onClick(VKApiCommunity.Link link) {
        getPresenter().fireLinkClick(link);
    }

    @Override
    public void onLongClick(VKApiCommunity.Link link) {
        String[] items = {getString(R.string.edit), getString(R.string.delete)};
        new AlertDialog.Builder(getActivity())
                .setTitle(link.name)
                .setItems(items, (dialog, which) -> {
                    switch (which){
                        case 0:
                            getPresenter().fireLinkEditClick(link);
                            break;
                        case 1:
                            getPresenter().fireLinkDeleteClick(link);
                            break;
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}