package biz.dealnote.messenger.fragment.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.MessagesAdapter;
import biz.dealnote.messenger.fragment.search.criteria.MessageSeachCriteria;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.mvp.presenter.search.MessagesSearchPresenter;
import biz.dealnote.messenger.mvp.view.search.IMessagesSearchView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by admin on 28.06.2016.
 * phoenix
 */
public class MessagesSearchFragment extends AbsSearchFragment<MessagesSearchPresenter, IMessagesSearchView, Message>
        implements MessagesAdapter.OnMessageActionListener, IMessagesSearchView {

    public static MessagesSearchFragment newInstance(int accountId, @Nullable MessageSeachCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        MessagesSearchFragment fragment = new MessagesSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Message> data) {
        ((MessagesAdapter) adapter).setItems(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Message> data) {
        MessagesAdapter adapter = new MessagesAdapter(getActivity(), data, this);
        //adapter.setOnHashTagClickListener(this);
        adapter.setOnMessageActionListener(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        getPresenter().fireOwnerClick(userId);
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
        // delete is not supported
    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message) {
        return false;
    }

    @Override
    public void onMessageClicked(@NonNull Message message) {
        getPresenter().fireMessageClick(message);
    }

    @Override
    public IPresenterFactory<MessagesSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            MessageSeachCriteria c = getArguments().getParcelable(Extra.CRITERIA);
            return new MessagesSearchPresenter(accountId, c, saveInstanceState);
        };
    }

    @Override
    protected String tag() {
        return MessagesSearchFragment.class.getSimpleName();
    }

    @Override
    public void goToMessagesLookup(int accountId, int peerId, int messageId) {
        PlaceFactory.getMessagesLookupPlace(accountId, peerId, messageId).tryOpenWith(getActivity());
    }
}
