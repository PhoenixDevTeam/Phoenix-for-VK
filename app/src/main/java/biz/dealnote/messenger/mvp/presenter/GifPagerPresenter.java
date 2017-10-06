package biz.dealnote.messenger.mvp.presenter;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.media.gif.IGifPlayer;
import biz.dealnote.messenger.media.gif.PlayerPrepareException;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.VideoSize;
import biz.dealnote.messenger.mvp.view.IGifPagerView;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.mvp.reflect.OnGuiCreated;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public class GifPagerPresenter extends BaseDocumentPresenter<IGifPagerView> implements IGifPlayer.IStatusChangeListener, IGifPlayer.IVideoSizeChangeListener {

    private static final String TAG = GifPagerPresenter.class.getSimpleName();
    private static final String SAVE_PAGER_INDEX = "save_pager_index";

    private IGifPlayer mGifPlayer;
    private ArrayList<Document> mDocuments;
    private int mCurrentIndex;

    public GifPagerPresenter(int accountId, @NonNull ArrayList<Document> documents, int index, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.mDocuments = documents;

        if (savedInstanceState == null) {
            mCurrentIndex = index;
        } else {
            mCurrentIndex = savedInstanceState.getInt(SAVE_PAGER_INDEX);
        }

        initGifPlayer();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_PAGER_INDEX, mCurrentIndex);
    }

    @OnGuiCreated
    private void resolveData() {
        if (isGuiReady()) {
            getView().displayData(mDocuments.size(), mCurrentIndex);
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireSurfaceCreated(int adapterPosition) {
        if (mCurrentIndex == adapterPosition) {
            resolvePlayerDisplay();
        }
    }

    @OnGuiCreated
    private void resolveToolbarTitle() {
        if (isGuiReady()) {
            getView().setToolbarTitle(R.string.gif_player);
        }
    }

    @OnGuiCreated
    private void resolveToolbarSubtitle() {
        if (isGuiReady()) {
            getView().setToolbarSubtitle(R.string.image_number, mCurrentIndex + 1, mDocuments.size());
        }
    }

    @OnGuiCreated
    private void resolvePlayerDisplay() {
        if (isGuiReady()) {
            getView().attachDisplayToPlayer(mCurrentIndex, mGifPlayer);
        } else {
            mGifPlayer.setDisplay(null);
        }
    }

    private void initGifPlayer() {
        if (Objects.nonNull(mGifPlayer)) {
            IGifPlayer old = mGifPlayer;
            mGifPlayer = null;
            old.release();
        }

        Document document = mDocuments.get(mCurrentIndex);
        AssertUtils.requireNonNull(document);

        final String url = document.getVideoPreview().getSrc();
        mGifPlayer = Injection.provideGifPlayerFactory().createGifPlayer(url);
        //mGifPlayer = new DefaultGifPlayer(document.getVideoPreview().getSrc());
        mGifPlayer.addStatusChangeListener(this);
        mGifPlayer.addVideoSizeChangeListener(this);

        try {
            mGifPlayer.play();
        } catch (PlayerPrepareException e) {
            safeShowLongToast(getView(), R.string.unable_to_play_file);
        }
    }

    private void selectPage(int position) {
        if (mCurrentIndex == position) {
            return;
        }

        mCurrentIndex = position;
        initGifPlayer();
    }

    @OnGuiCreated
    private void resolveAddDeleteButton() {
        if (isGuiReady()) {
            getView().setupAddRemoveButton(!isMy());
        }
    }

    private boolean isMy() {
        return mDocuments.get(mCurrentIndex).getOwnerId() == getAccountId();
    }

    @OnGuiCreated
    private void resolveAspectRatio() {
        if (isGuiReady()) {
            VideoSize size = mGifPlayer.getVideoSize();
            if (size != null) {
                getView().setAspectRatioAt(mCurrentIndex, size.getWidth(), size.getHeight());
            }
        }
    }

    @OnGuiCreated
    private void resolvePreparingProgress() {
        boolean preparing = !Objects.isNull(mGifPlayer) && mGifPlayer.getPlayerStatus() == IGifPlayer.IStatus.PREPARING;
        if (isGuiReady()) {
            getView().setPreparingProgressVisible(mCurrentIndex, preparing);
        }
    }

    public void firePageSelected(int position) {
        if(mCurrentIndex == position){
            return;
        }

        selectPage(position);
        resolveToolbarSubtitle();
        resolvePreparingProgress();
    }

    public void fireAddDeleteButtonClick() {
        Document document = mDocuments.get(mCurrentIndex);
        if (isMy()) {
            delete(document.getId(), document.getOwnerId());
        } else {
            addYourself(document);
        }
    }

    private static final VideoSize DEF_SIZE = new VideoSize(1, 1);

    public void fireHolderCreate(int adapterPosition) {
        boolean isProgress = adapterPosition == mCurrentIndex && mGifPlayer.getPlayerStatus() == IGifPlayer.IStatus.PREPARING;

        VideoSize size = mGifPlayer.getVideoSize();
        if(Objects.isNull(size)){
            size = DEF_SIZE;
        }

        getView().configHolder(adapterPosition, isProgress, size.getWidth(), size.getWidth());
    }

    public void fireShareButtonClick() {
        getView().shareDocument(getAccountId(), mDocuments.get(mCurrentIndex));
    }

    public void fireDownloadButtonClick() {
        if(!AppPerms.hasWriteStoragePermision(App.getInstance())){
            getView().requestWriteExternalStoragePermission();
            return;
        }

        downloadImpl();
    }

    @Override
    public void onWritePermissionResolved() {
        if(AppPerms.hasWriteStoragePermision(App.getInstance())){
            downloadImpl();
        }
    }

    @Override
    public void onGuiPaused() {
        super.onGuiPaused();
        if(Objects.nonNull(mGifPlayer)){
            mGifPlayer.pause();
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();

        if(Objects.nonNull(mGifPlayer)){
            try {
                mGifPlayer.play();
            } catch (PlayerPrepareException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyed() {
        if(Objects.nonNull(mGifPlayer)){
            mGifPlayer.release();
        }
        super.onDestroyed();
    }

    private void downloadImpl(){
        Document document = mDocuments.get(mCurrentIndex);

        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(document.getUrl()));
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, document.getTitle());
        req.allowScanningByMediaScanner();
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager dm = (DownloadManager) App.getInstance().getSystemService(Context.DOWNLOAD_SERVICE);
        dm.enqueue(req);
    }

    @Override
    public void onPlayerStatusChange(@NonNull IGifPlayer player, int previousStatus, int currentStatus) {
        if(mGifPlayer == player){
            resolvePreparingProgress();
            resolvePlayerDisplay();
        }
    }

    @Override
    public void onVideoSizeChanged(@NonNull IGifPlayer player, VideoSize size) {
        if (mGifPlayer == player) {
            resolveAspectRatio();
        }
    }
}