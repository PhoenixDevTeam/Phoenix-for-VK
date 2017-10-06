package biz.dealnote.messenger.dialog.base;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.adapter.AttachmentsViewBinder;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.WikiPage;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.settings.Settings;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class AccountDependencyDialogFragment extends BaseDialogFragment
        implements AttachmentsViewBinder.OnAttachmentsActionCallback {

    private int accountId;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getArguments().containsKey(Extra.ACCOUNT_ID)) {
            throw new IllegalArgumentException("Fragments args does not constains Extra.ACCOUNT_ID");
        }

        accountId = getArguments().getInt(Extra.ACCOUNT_ID);
        mCompositeDisposable.add(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::fireAccountChange));
    }

    private void fireAccountChange(int newAid) {
        int oldAid = accountId;

        if (!supportAccountHotSwap) {
            if (newAid != oldAid) {
                setInvalidAccountContext(true);
                onAccountContextInvalidState();
            } else {
                setInvalidAccountContext(false);
            }

            return;
        }

        if (newAid == oldAid) return;

        beforeAccountChange(oldAid, newAid);

        AccountDependencyDialogFragment.this.accountId = newAid;
        getArguments().putInt(Extra.ACCOUNT_ID, newAid);

        afterAccountChange(oldAid, newAid);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    protected void appendDisposable(Disposable disposable){
        this.mCompositeDisposable.add(disposable);
    }

    protected void afterAccountChange(int oldAid, int newAid) {

    }

    protected void beforeAccountChange(int oldAid, int newAid) {

    }

    protected final int getAccountId() {
        return accountId;
    }

    @Override
    public void onPollOpen(@NonNull Poll poll) {
        ///PlaceManager.withContext(getContext())
        //        .toPoll()
        //        .withArguments(PollDialog.buildArgs(getAccountId(), poll, true))
        //       .open();
    }

    @Override
    public void onVideoPlay(@NonNull Video video) {
        PlaceFactory.getVideoPreviewPlace(getAccountId(), video).tryOpenWith(getActivity());
    }

    @Override
    public void onAudioPlay(int position, @NonNull ArrayList<Audio> audios) {
        MusicPlaybackService.startForPlayList(getActivity(), audios, position, false);
        PlaceFactory.getPlayerPlace(getAccountId()).tryOpenWith(getActivity());
    }

    @Override
    public void onForwardMessagesOpen(@NonNull ArrayList<Message> messages) {
        PlaceFactory.getForwardMessagesPlace(getAccountId(), messages).tryOpenWith(getActivity());
    }

    @Override
    public void onOpenOwner(int userId) {
        PlaceFactory.getOwnerWallPlace(getAccountId(), userId, null).tryOpenWith(getActivity());
    }

    @Override
    public void onDocPreviewOpen(@NonNull Document document) {
        PlaceFactory.getDocPreviewPlace(getAccountId(), document).tryOpenWith(getActivity());
    }

    @Override
    public void onPostOpen(@NonNull Post post) {
        PlaceFactory.getPostPreviewPlace(getAccountId(), post.getVkid(), post.getOwnerId(), post).tryOpenWith(getActivity());
    }

    @Override
    public void onLinkOpen(@NonNull Link link) {
        LinkHelper.openLinkInBrowser(getActivity(), link.getUrl());
    }

    @Override
    public void onWikiPageOpen(@NonNull WikiPage page) {
        PlaceFactory.getWikiPagePlace(getAccountId(), page.getViewUrl()).tryOpenWith(getActivity());
    }

    @Override
    public void onPhotosOpen(@NonNull ArrayList<Photo> photos, int index) {
        PlaceFactory.getSimpleGalleryPlace(getAccountId(), photos, index, true).tryOpenWith(getActivity());
    }

    protected void onAccountContextInvalidState() {
        if (isAdded() && isResumed()) {
            getFragmentManager().popBackStack();
        }
    }

    private static final String ARGUMENT_INVALID_ACCOUNT_CONTEXT = "invalid_account_context";

    @Override
    public void onResume() {
        super.onResume();
        if (isInvalidAccountContext()) {
            getFragmentManager().popBackStack();
        }
    }

    private boolean supportAccountHotSwap;

    @SuppressWarnings("unused")
    public boolean isSupportAccountHotSwap() {
        return supportAccountHotSwap;
    }

    public void setSupportAccountHotSwap(boolean supportAccountHotSwap) {
        this.supportAccountHotSwap = supportAccountHotSwap;
    }

    protected void setInvalidAccountContext(boolean invalidAccountContext) {
        getArguments().putBoolean(ARGUMENT_INVALID_ACCOUNT_CONTEXT, invalidAccountContext);
    }

    public boolean isInvalidAccountContext() {
        return getArguments().getBoolean(ARGUMENT_INVALID_ACCOUNT_CONTEXT);
    }
}
