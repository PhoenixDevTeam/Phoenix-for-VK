package biz.dealnote.messenger.upload.experimental;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.util.Collections;

import biz.dealnote.messenger.api.PercentageListener;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.db.interfaces.IMessagesStorage;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.upload.task.AbstractUploadTask;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Utils.safelyClose;

public class Photo2MessageUploadable extends AbstractUploadable<Photo> {

    private final Context context;
    private final INetworker networker;
    private final IAttachmentsRepository attachmentsRepository;
    private final IMessagesStorage messagesStore;

    public Photo2MessageUploadable(Context context, INetworker networker, IAttachmentsRepository attachmentsRepository, IMessagesStorage messagesStore) {
        this.context = context;
        this.networker = networker;
        this.attachmentsRepository = attachmentsRepository;
        this.messagesStore = messagesStore;
    }

    @Override
    Single<UploadServer> obtainServer(@NonNull UploadObject upload) {
        return networker.vkDefault(upload.getAccountId())
                .photos()
                .getMessagesUploadServer().map(s -> s);
    }

    @Override
    Completable commit(@NonNull UploadObject upload, @NonNull Photo result) {
        int accountId = upload.getAccountId();
        int messageId = upload.getDestination().getId();
        return attachmentsRepository.attach(upload.getAccountId(), AttachToType.MESSAGE, messageId, Collections.singletonList(result))
                .andThen(messagesStore.notifyMessageHasAttachments(accountId, messageId));
    }

    @Override
    Single<Photo> doUpload(@NonNull UploadServer server, @NonNull UploadObject upload, @Nullable PercentageListener listener) {
        final InputStream[] is = new InputStream[1];

        try {
            is[0] = AbstractUploadTask.openStream(context, upload.getFileUri(), upload.getSize());
            return networker.uploads()
                    .uploadPhotoToMessageRx(server.getUrl(), is[0], listener)
                    .doFinally(() -> safelyClose(is[0]))
                    .flatMap(dto -> networker.vkDefault(upload.getAccountId())
                            .photos()
                            .saveMessagesPhoto(dto.server, dto.photo, dto.hash)
                            .flatMap(photos -> {
                                if(photos.isEmpty()){
                                    return Single.error(new NotFoundException());
                                }

                                return Single.just(Dto2Model.transform(photos.get(0)));
                            }));
        } catch (Exception e){
            return Single.error(e);
        } finally {
            safelyClose(is[0]);
        }
    }
}