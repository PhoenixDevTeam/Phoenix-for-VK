package biz.dealnote.messenger.mvp.presenter.history;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.response.AttachmentsHistoryResponse;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.db.serialize.Serializers;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.TmpSource;
import biz.dealnote.messenger.mvp.view.IChatAttachmentPhotosView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.DisposableHolder;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public class ChatAttachmentPhotoPresenter extends BaseChatAttachmentsPresenter<Photo, IChatAttachmentPhotosView> {

    public ChatAttachmentPhotoPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    Single<Pair<String, List<Photo>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, "photo", nextFrom, 50, null)
                .map(response -> {
                    List<Photo> photos = new ArrayList<>();

                    for (AttachmentsHistoryResponse.One one : response.items) {
                        if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiPhoto) {
                            VKApiPhoto dto = (VKApiPhoto) one.entry.attachment;
                            photos.add(Dto2Model.transform(dto));
                        }
                    }

                    return Pair.create(response.next_from, photos);
                });
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.photos_count, safeCountOf(data)));
        }
    }

    @Override
    protected String tag() {
        return ChatAttachmentPhotoPresenter.class.getSimpleName();
    }

    private DisposableHolder<Void> openGalleryDisposableHolder = new DisposableHolder<>();

    @Override
    public void onDestroyed() {
        openGalleryDisposableHolder.dispose();
        super.onDestroyed();
    }

    @SuppressWarnings("unused")
    public void firePhotoClick(int position, Photo photo) {
        final List<Photo> photos = super.data;

        TmpSource source = new TmpSource(getInstanceId(), 0);

        fireTempDataUsage();

        openGalleryDisposableHolder.append(Stores.getInstance()
                .tempStore()
                .put(source.getOwnerId(), source.getSourceId(), data, Serializers.PHOTOS_SERIALIZER)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onPhotosSavedToTmpStore(position, source), Analytics::logUnexpectedError));
    }

    private void onPhotosSavedToTmpStore(int index, TmpSource source) {
        callView(view -> view.goToTempPhotosGallery(getAccountId(), source, index));
    }
}