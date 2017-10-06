package biz.dealnote.messenger.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.DialogPreviewAdapter;
import biz.dealnote.messenger.fragment.search.criteria.DialogsSearchCriteria;
import biz.dealnote.messenger.mvp.presenter.search.DialogsSearchPresenter;
import biz.dealnote.messenger.mvp.view.search.IDialogsSearchView;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public class DialogsSearchFragment extends AbsSearchFragment<DialogsSearchPresenter, IDialogsSearchView, Object>
        implements IDialogsSearchView, DialogPreviewAdapter.ActionListener {

    public static DialogsSearchFragment newInstance(int accountId, DialogsSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        DialogsSearchFragment fragment = new DialogsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public IPresenterFactory<DialogsSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            DialogsSearchCriteria criteria = getArguments().getParcelable(Extra.CRITERIA);
            return new DialogsSearchPresenter(accountId, criteria, saveInstanceState);
        };
    }

    @Override
    protected String tag() {
        return DialogsSearchFragment.class.getSimpleName();
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Object> data) {
        ((DialogPreviewAdapter) adapter).setData(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Object> data) {
        return new DialogPreviewAdapter(getActivity(), data, this);
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    public void onEntryClick(Object o) {
        getPresenter().fireEntryClick(o);
    }
}