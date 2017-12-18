package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.activity.SendAttachmentsActivity;
import biz.dealnote.messenger.adapter.MessagesAdapter;
import biz.dealnote.messenger.fragment.base.PlaceSupportPresenterFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.model.FwdMessages;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.mvp.presenter.MessagesLookPresenter;
import biz.dealnote.messenger.mvp.view.IMessagesLookView;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.view.LoadMoreFooterHelper;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by ruslan.kolbasa on 03.10.2016.
 * phoenix
 */
public class MessagesLookFragment extends PlaceSupportPresenterFragment<MessagesLookPresenter, IMessagesLookView>
        implements IMessagesLookView, MessagesAdapter.OnMessageActionListener {

    private static final String TAG = MessagesLookFragment.class.getSimpleName();
    private static final int REQUEST_FORWARD_MESSAGE = 1566;

    private RecyclerView mRecyclerView;
    private MessagesAdapter mMessagesAdapter;

    public static Bundle buildArgs(int accountId, int peerId, int focusMesssageId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.PEER_ID, peerId);
        args.putInt(Extra.FOCUS_TO, focusMesssageId);
        return args;
    }

    public static MessagesLookFragment newInstance(Bundle args) {
        MessagesLookFragment fragment = new MessagesLookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private View mHeaderView;
    private View mFooterView;

    private LoadMoreFooterHelper mHeaderHelper;
    private LoadMoreFooterHelper mFooterHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_messages_lookup, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, true);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(layoutManager);

        mHeaderView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mFooterView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mHeaderHelper = LoadMoreFooterHelper.createFrom(mHeaderView, this::onHeaderLoadMoreClick);
        mFooterHelper = LoadMoreFooterHelper.createFrom(mFooterView, this::onFooterLoadMoreClick);

        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                Logger.d(TAG, "onScrollToLastElement");
            }

            @Override
            public void onScrollToFirstElement() {
                Logger.d(TAG, "onScrollToFirstElement");
            }
        };

        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
        return root;
    }

    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;

    private void onFooterLoadMoreClick() {
        getPresenter().fireFooterLoadMoreClick();
    }

    private void onHeaderLoadMoreClick() {
        getPresenter().fireHeaderLoadMoreClick();
    }

    @Override
    public void displayMessages(@NonNull List<Message> messages) {
        mMessagesAdapter = new MessagesAdapter(getActivity(), messages, this);
        mMessagesAdapter.setOnMessageActionListener(this);
        mMessagesAdapter.addFooter(mHeaderView);
        mMessagesAdapter.addHeader(mFooterView);
        mRecyclerView.setAdapter(mMessagesAdapter);
    }

    @Override
    public void focusTo(int index) {
        mRecyclerView.removeOnScrollListener(mEndlessRecyclerOnScrollListener);
        mRecyclerView.scrollToPosition(index + 1); // +header
        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    @Override
    public void notifyMessagesUpAdded(int startPosition, int count) {
        if(Objects.nonNull(mMessagesAdapter)){
            mMessagesAdapter.notifyItemRangeInserted(startPosition + 1, count); //+header
        }
    }

    @Override
    public void notifyMessagesDownAdded(int count) {
        if(Objects.nonNull(mMessagesAdapter)){
            mMessagesAdapter.notifyItemRemoved(0);
            mMessagesAdapter.notifyItemRangeInserted(0, count + 1); //+header
        }
    }

    @Override
    public void configNowVoiceMessagePlaying(int id, float progress, boolean paused, boolean amin) {
        // TODO: 09.10.2016
    }

    @Override
    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin) {
        // TODO: 09.10.2016
    }

    @Override
    public void disableVoicePlaying() {
        // TODO: 09.10.2016
    }

    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    getPresenter().fireActionModeDeleteClick();
                    break;
                case R.id.copy:
                    getPresenter().fireActionModeCopyClick();
                    break;
                case R.id.forward:
                    getPresenter().fireForwardClick();
                    break;
            }

            mode.finish();
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.messages_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getPresenter().fireActionModeDestroy();
            mActionMode = null;
        }
    }

    @Override
    public void showActionMode(String title) {
        if (Objects.isNull(mActionMode)) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
        }

        if(Objects.nonNull(mActionMode)){
            mActionMode.setTitle(title);
            mActionMode.invalidate();
        }
    }

    @Override
    public void finishActionMode() {
        if(Objects.nonNull(mActionMode)){
            mActionMode.finish();
            mActionMode = null;
        }
    }

    @Override
    public void notifyDataChanged() {
        if(Objects.nonNull(mMessagesAdapter)){
            mMessagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setupHeaders(@LoadMoreState int upHeaderState, @LoadMoreState int downHeaderState) {
        if(Objects.nonNull(mHeaderHelper)){
            mHeaderHelper.switchToState(upHeaderState);
        }

        if(Objects.nonNull(mFooterHelper)){
            mFooterHelper.switchToState(downHeaderState);
        }
    }

    @Override
    public void forwardMessages(int accountId, @NonNull ArrayList<Message> messages) {
        SendAttachmentsActivity.startForSendAttachments(getActivity(), accountId, new FwdMessages(messages));
        //App.getInstance().setFwdBuffer(messages);
        //SendAttachmentsActivity.startForForwardMessages(this, REQUEST_FORWARD_MESSAGE, accountId);
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_FORWARD_MESSAGE){
            App.getInstance().setFwdBuffer(null);
            if(isPresenterPrepared()){
                getPresenter().fireMessageForwardSent();
            }
        }
    }*/

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    public void savePresenterState(@NonNull MessagesLookPresenter presenter, @NonNull Bundle outState) {
        presenter.saveState(outState);
    }

    @Override
    public IPresenterFactory<MessagesLookPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = getArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = getArguments().getInt(Extra.PEER_ID);
            Integer focusTo = getArguments().containsKey(Extra.FOCUS_TO) ? getArguments().getInt(Extra.FOCUS_TO) : null;
            return new MessagesLookPresenter(aid, peerId, focusTo, saveInstanceState);
        };
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        if(Objects.nonNull(mActionMode)){
            getPresenter().fireMessageClick(message);
        } else {
            getPresenter().fireOwnerClick(userId);
        }
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
        getPresenter().fireMessageRestoreClick(message, position);
    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message) {
        getPresenter().fireMessageLongClick(message);
        return true;
    }

    @Override
    public void onMessageClicked(@NonNull Message message) {
        getPresenter().fireMessageClick(message);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(Objects.nonNull(actionBar)){
            actionBar.setTitle(R.string.viewing_messages);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }
}
