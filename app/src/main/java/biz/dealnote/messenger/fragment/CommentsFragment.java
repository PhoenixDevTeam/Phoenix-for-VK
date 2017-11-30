package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.adapter.CommentsAdapter;
import biz.dealnote.messenger.adapter.OwnersListAdapter;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.fragment.base.PlaceSupportPresenterFragment;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.presenter.CommentsPresenter;
import biz.dealnote.messenger.mvp.view.ICommentsView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.RoundTransformation;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.view.CommentsInputViewController;
import biz.dealnote.messenger.view.LoadMoreFooterHelper;
import biz.dealnote.messenger.view.emoji.EmojiconTextView;
import biz.dealnote.messenger.view.emoji.StickersGridView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by Ruslan Kolbasa on 07.06.2017.
 * phoenix
 */
public class CommentsFragment extends PlaceSupportPresenterFragment<CommentsPresenter, ICommentsView> implements
        ICommentsView, StickersGridView.OnStickerClickedListener, CommentsInputViewController.OnInputActionCallback,
        CommentsAdapter.OnCommentActionListener, EmojiconTextView.OnHashTagClickListener, BackPressCallback {

    private static final String EXTRA_AT_COMMENT_OBJECT = "at_comment_object";

    private static final int REQUEST_CODE_ATTACHMENTS = 17;
    private static final int REQUEST_EDIT = 18;

    public static CommentsFragment newInstance(@NonNull Place place) {
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(place.getArgs());
        return fragment;
    }

    public static Bundle buildArgs(int accountId, Commented commented, Integer focusToComment) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putParcelable(Extra.COMMENTED, commented);
        if (focusToComment != null) {
            bundle.putInt(EXTRA_AT_COMMENT_OBJECT, focusToComment);
        }

        return bundle;
    }

    private CommentsInputViewController mInputController;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private ViewGroup mReplyView;
    private TextView mReplyText;

    private LoadMoreFooterHelper upHelper;
    private LoadMoreFooterHelper downhelper;

    private CommentsAdapter mAdapter;

    private ProgressBar mCenterProgressBar;
    private View mEmptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private ImageView mAuthorAvatar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (RelativeLayout) inflater.inflate(R.layout.fragment_comments, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mAuthorAvatar = root.findViewById(R.id.author_avatar);

        mInputController = new CommentsInputViewController(getActivity(), root, this);
        mInputController.setOnSickerClickListener(this);

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true);

        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mReplyView = root.findViewById(R.id.fragment_comments_reply_container);
        mReplyText = root.findViewById(R.id.fragment_comments_reply_user);

        root.findViewById(R.id.fragment_comments_delete_reply).setOnClickListener(v -> getPresenter().fireReplyCancelClick());

        View loadUpView = inflater.inflate(R.layout.footer_load_more_test, mRecyclerView, false);
        upHelper = LoadMoreFooterHelper.createFrom(loadUpView, () -> getPresenter().fireUpLoadMoreClick());
        //upHelper.setEndOfListText("•••••••••");
        upHelper.setEndOfListText(" ");

        View loadDownView = inflater.inflate(R.layout.footer_load_more_test, mRecyclerView, false);
        downhelper = LoadMoreFooterHelper.createFrom(loadDownView, () -> getPresenter().fireDownLoadMoreClick());
        //downhelper.setEndOfListText("•••••••••");
        downhelper.setEndOfListTextRes(R.string.place_for_your_comment);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToTop();
            }
        });

        mAdapter = new CommentsAdapter(getActivity(), Collections.emptyList(), this);
        mAdapter.addHeader(loadDownView);
        mAdapter.addFooter(loadUpView);
        mAdapter.setListener(this);
        mAdapter.setOnHashTagClickListener(this);

        mRecyclerView.setAdapter(mAdapter);

        mCenterProgressBar = root.findViewById(R.id.progress_bar);
        mEmptyView = root.findViewById(R.id.empty_text);
        return root;
    }

    @Override
    public boolean onSendLongClick() {
        if (mCanSendCommentAsAdmin) {
            getPresenter().fireSendLongClick();
            //PopupMenu popupMenu = new PopupMenu(getActivity(), mInputController.getButtonSendView());
            //popupMenu.getMenu()
            //        .add(R.string.button_post_as_group)
            //        .setOnMenuItemClickListener(item -> {
            //            getPresenter().firePostAsGroupClick();
            //            return true;
            //        });
            //popupMenu.show();
            return true;
        }

        return false;
    }

    @Override
    public IPresenterFactory<CommentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            Commented commented = getArguments().getParcelable(Extra.COMMENTED);

            Integer focusTo = null;
            if (getArguments().containsKey(EXTRA_AT_COMMENT_OBJECT)) {
                focusTo = getArguments().getInt(EXTRA_AT_COMMENT_OBJECT);
                getArguments().remove(EXTRA_AT_COMMENT_OBJECT);
            }

            return new CommentsPresenter(accountId, commented, focusTo, saveInstanceState);
        };
    }

    @Override
    protected String tag() {
        return CommentsFragment.class.getSimpleName();
    }

    @Override
    public void displayData(List<Comment> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(data);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setupLoadUpHeader(int state) {
        if (nonNull(upHelper)) {
            upHelper.switchToState(state);
        }
    }

    @Override
    public void setupLoadDownFooter(int state) {
        if (nonNull(downhelper)) {
            downhelper.switchToState(state);
        }
    }

    @Override
    public void notifyDataAddedToTop(int count) {
        if (nonNull(mAdapter)) {
            int startSize = mAdapter.getRealItemCount();
            mAdapter.notifyItemRangeInserted(startSize + mAdapter.getHeadersCount(), count);
        }
    }

    @Override
    public void notifyDataAddedToBottom(int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(0);
            mAdapter.notifyItemRangeInserted(0, count + 1);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index + mAdapter.getHeadersCount());
        }
    }

    @Override
    public void moveFocusTo(int index, boolean smooth) {
        if (isNull(mAdapter)) {
            return;
        }

        int adapterPosition = index + mAdapter.getHeadersCount();
        if (smooth) {
            if (nonNull(mRecyclerView)) {
                mRecyclerView.smoothScrollToPosition(adapterPosition);
            }
        } else {
            if (nonNull(mLinearLayoutManager)) {
                mLinearLayoutManager.scrollToPosition(adapterPosition);
            }
        }
    }

    @Override
    public void displayBody(String body) {
        if (nonNull(mInputController)) {
            mInputController.setTextQuietly(body);
        }
    }

    @Override
    public void displayAttachmentsCount(int count) {
        if (nonNull(mInputController)) {
            mInputController.setAttachmentsCount(count);
        }
    }

    @Override
    public void setButtonSendAvailable(boolean available) {
        if (nonNull(mInputController)) {
            mInputController.setCanSendNormalMessage(available);
        }
    }

    @Override
    public void openAttachmentsManager(int accountId, Integer draftCommentId, int sourceOwnerId, String draftCommentBody) {
        PlaceFactory.getCommentCreatePlace(accountId, draftCommentId, sourceOwnerId, draftCommentBody)
                .targetTo(this, REQUEST_CODE_ATTACHMENTS)
                .tryOpenWith(getActivity());
    }

    @Override
    public void setupReplyViews(String replyTo) {
        if (nonNull(mReplyView)) {
            mReplyView.setVisibility(nonNull(replyTo) ? View.VISIBLE : View.GONE);
        }

        if (nonNull(replyTo) && nonNull(mReplyText)) {
            mReplyText.setText(replyTo);
        }
    }

    @Override
    public void replaceBodySelectionTextTo(String replyText) {
        if (nonNull(mInputController)) {
            EditText edit = mInputController.getInputField();

            int selectionStart = edit.getSelectionStart();
            int selectionEnd = edit.getSelectionEnd();
            edit.getText().replace(selectionStart, selectionEnd, replyText);
        }
    }

    @Override
    public void goToCommentEdit(int accountId, Comment comment) {
        PlaceFactory.getEditCommentPlace(accountId, comment)
                .targetTo(this, REQUEST_EDIT)
                .tryOpenWith(getActivity());
    }

    @Override
    public void goToWallPost(int accountId, int postId, int postOwnerId) {
        PlaceFactory.getPostPreviewPlace(accountId, postId, postOwnerId).tryOpenWith(getActivity());
    }

    @Override
    public void goToVideoPreview(int accountId, int videoId, int videoOwnerId) {
        PlaceFactory.getVideoPreviewPlace(accountId, videoOwnerId, videoId, null).tryOpenWith(getActivity());
    }

    @Override
    public void banUser(int accountId, int groupId, User user) {
        PlaceFactory.getCommunityAddBanPlace(accountId, groupId, Utils.singletonArrayList(user)).tryOpenWith(getActivity());
    }

    @Override
    public void displayAuthorAvatar(String url) {
        if(nonNull(mAuthorAvatar)){
            if(nonEmpty(url)){
                mAuthorAvatar.setVisibility(View.VISIBLE);

                PicassoInstance.with()
                        .load(url)
                        .transform(new RoundTransformation())
                        .into(mAuthorAvatar);
            } else {
                mAuthorAvatar.setVisibility(View.GONE);
                PicassoInstance.with()
                        .cancelRequest(mAuthorAvatar);
            }
        }
    }

    @Override
    public void showAuthorSelectDialog(List<Owner> owners) {
        final ArrayList<Owner> data = new ArrayList<>(owners);
        OwnersListAdapter adapter = new OwnersListAdapter(getActivity(), data);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_comment_author)
                .setAdapter(adapter, (dialog, which) -> getPresenter().fireAuthorSelected(data.get(which)))
                .setNegativeButton(R.string.button_cancel, null)
                .show();

    }

    @Override
    public void setupOptionMenu(boolean topicPollAvailable, boolean gotoSourceAvailable, Integer gotoSourceText) {
        this.mTopicPollAvailable = topicPollAvailable;
        this.mGotoSourceAvailable = gotoSourceAvailable;
        this.mGotoSourceText = gotoSourceText;

        try {
            getActivity().invalidateOptionsMenu();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setEpmtyTextVisible(boolean visible) {
        if (nonNull(mEmptyView)) {
            mEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setCenterProgressVisible(boolean visible) {
        if (nonNull(mCenterProgressBar)) {
            mCenterProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private ProgressDialog mDeepLookingProgressDialog;

    @Override
    public void displayDeepLookingCommentProgress() {
        mDeepLookingProgressDialog = new ProgressDialog(getActivity());
        mDeepLookingProgressDialog.setMessage(getString(R.string.please_wait));
        mDeepLookingProgressDialog.setCancelable(true);
        mDeepLookingProgressDialog.setOnCancelListener(dialog -> getPresenter().fireDeepLookingCancelledByUser());
        mDeepLookingProgressDialog.show();
    }

    @Override
    public void dismissDeepLookingCommentProgress() {
        if (nonNull(mDeepLookingProgressDialog)) {
            mDeepLookingProgressDialog.dismiss();
        }
    }

    private boolean mCanSendCommentAsAdmin;

    @Override
    public void setCanSendSelectAuthor(boolean can) {
        this.mCanSendCommentAsAdmin = can;
    }

    @Override
    public void onStickerClick(int stickerId) {
        getPresenter().fireStickerClick(stickerId);
    }

    @Override
    public void onInputTextChanged(String s) {
        getPresenter().fireInputTextChanged(s);
    }

    @Override
    public void onSendClicked() {
        getPresenter().fireSendClick();
    }

    @Override
    public void onAttachClick() {
        getPresenter().fireAttachClick();
    }

    @Override
    public void onReplyToOwnerClick(int ownerId, int commentId) {
        getPresenter().fireReplyToOwnerClick(commentId);
    }

    @Override
    public void onRestoreComment(int commentId) {
        getPresenter().fireCommentRestoreClick(commentId);
    }

    @Override
    public void onAvatarClick(int ownerId) {
        super.onOpenOwner(ownerId);
    }

    @Override
    public void onCommentLikeClick(Comment comment, boolean add) {
        getPresenter().fireCommentLikeClick(comment, add);
    }

    private static final class ContextView implements ICommentContextView {

        boolean canEdit;

        boolean canDelete;

        boolean canBan;

        @Override
        public void setCanEdit(boolean can) {
            this.canEdit = can;
        }

        @Override
        public void setCanDelete(boolean can) {
            this.canDelete = can;
        }

        @Override
        public void setCanBan(boolean can) {
            this.canBan = can;
        }
    }

    @Override
    public void populateCommentContextMenu(ContextMenu menu, Comment comment) {
        ContextView contextView = new ContextView();
        getPresenter().fireCommentContextViewCreated(contextView, comment);

        menu.setHeaderTitle(comment.getFullAuthorName());

        menu.add(R.string.reply).setOnMenuItemClickListener(item -> {
            getPresenter().fireReplyToCommentClick(comment);
            return true;
        });

        if (contextView.canDelete) {
            menu.add(R.string.delete)
                    .setOnMenuItemClickListener(item -> {
                        getPresenter().fireCommentDeleteClick(comment);
                        return true;
                    });
        }

        if (contextView.canEdit) {
            menu.add(R.string.edit)
                    .setOnMenuItemClickListener(item -> {
                        getPresenter().fireCommentEditClick(comment);
                        return true;
                    });
        }

        if (contextView.canBan) {
            menu.add(R.string.ban_author)
                    .setOnMenuItemClickListener(item -> {
                        getPresenter().fireBanClick(comment);
                        return true;
                    });
        }

        menu.add(R.string.like)
                .setVisible(!comment.isUserLikes())
                .setOnMenuItemClickListener(item -> {
                    getPresenter().fireCommentLikeClick(comment, true);
                    return true;
                });

        menu.add(R.string.dislike)
                .setVisible(comment.isUserLikes())
                .setOnMenuItemClickListener(item -> {
                    getPresenter().fireCommentLikeClick(comment, false);
                    return true;
                });

        menu.add(R.string.who_likes)
                .setOnMenuItemClickListener(item -> {
                    getPresenter().fireWhoLikesClick(comment);
                    return true;
                });
    }

    @Override
    public void onHashTagClicked(String hashTag) {
        getPresenter().fireHashtagClick(hashTag);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ATTACHMENTS) {
            String body = data.getStringExtra(Extra.BODY);
            postPrenseterReceive(presenter -> presenter.fireEditBodyResult(body));
            //getPresenter().fireEditBodyResult(body);
        }

        if (requestCode == REQUEST_EDIT && resultCode == Activity.RESULT_OK) {
            Comment comment = data.getParcelableExtra(Extra.COMMENT);

            if (nonNull(comment)) {
                postPrenseterReceive(presenter -> presenter.fireCommentEditResult(comment));
                //getPresenter().fireCommentEditResult(comment);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.comments_list_menu, menu);
    }

    private boolean mTopicPollAvailable;
    private boolean mGotoSourceAvailable;

    @StringRes
    private Integer mGotoSourceText;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.open_poll).setVisible(mTopicPollAvailable);

        MenuItem gotoSource = menu.findItem(R.id.to_commented);
        gotoSource.setVisible(mGotoSourceAvailable);

        if (mGotoSourceAvailable) {
            gotoSource.setTitle(mGotoSourceText);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                getPresenter().fireRefreshClick();
                return true;

            case R.id.open_poll:
                getPresenter().fireTopicPollClick();
                return true;

            case R.id.to_commented:
                getPresenter().fireGotoSourceClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.d(tag(), "finalize");
        super.finalize();
    }

    @Override
    public boolean onBackPressed() {
        return mInputController.onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mInputController.destroyView();
        mInputController = null;
    }
}