package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.ProxiesAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.mvp.presenter.ProxyManagerPresenter;
import biz.dealnote.messenger.mvp.view.IProxyManagerView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 10.07.2017.
 * phoenix
 */
public class ProxyManagerFrgament extends BasePresenterFragment<ProxyManagerPresenter, IProxyManagerView>
        implements IProxyManagerView, ProxiesAdapter.ActionListener {

    public static ProxyManagerFrgament newInstance() {
        Bundle args = new Bundle();
        ProxyManagerFrgament fragment = new ProxyManagerFrgament();
        fragment.setArguments(args);
        return fragment;
    }

    private ProxiesAdapter mProxiesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frgament_proxy_manager, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mProxiesAdapter = new ProxiesAdapter(Collections.emptyList(), this);
        recyclerView.setAdapter(mProxiesAdapter);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.proxies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            getPresenter().fireAddClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public IPresenterFactory<ProxyManagerPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ProxyManagerPresenter(saveInstanceState);
    }

    @Override
    protected String tag() {
        return ProxyManagerFrgament.class.getSimpleName();
    }


    @Override
    public void displayData(List<ProxyConfig> configs, ProxyConfig active) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.setData(configs, active);
        }
    }

    @Override
    public void notifyItemAdded(int position) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.notifyItemInserted(position + mProxiesAdapter.getHeadersCount());
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.notifyItemRemoved(position + mProxiesAdapter.getHeadersCount());
        }
    }

    @Override
    public void setActiveAndNotifyDataSetChanged(ProxyConfig config) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.setActive(config);
        }
    }

    @Override
    public void goToAddingScreen() {
        PlaceFactory.getProxyAddPlace().tryOpenWith(getActivity());
    }

    @Override
    public void onDeleteClick(ProxyConfig config) {
        getPresenter().fireDeleteClick(config);
    }

    @Override
    public void onSetAtiveClick(ProxyConfig config) {
        getPresenter().fireActivateClick(config);
    }

    @Override
    public void onDisableClick(ProxyConfig config) {
        getPresenter().fireDisableClick(config);
    }
}