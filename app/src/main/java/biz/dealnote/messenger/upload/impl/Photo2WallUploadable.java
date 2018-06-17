package biz.dealnote.messenger.upload.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.util.Collections;

import biz.dealnote.messenger.api.PercentagePublisher;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.upload.IUploadable;
import biz.dealnote.messenger.upload.Method;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadResult;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.util.Objects;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.RxUtils.safelyCloseAction;
import static biz.dealnote.messenger.util.Utils.safelyClose;

public class Photo2WallUploadable implements IUploadable<Photo> {

    private final Context context;
    private final INetworker networker;
    private final IAttachmentsRepository attachmentsRepository;

    public Photo2WallUploadable(Context context, INetworker networker, IAttachmentsRepository attachmentsRepository) {
        this.context = context;
        this.networker = networker;
        this.attachmentsRepository = attachmentsRepository;
    }

    @Override
    public Single<UploadResult<Photo>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        int subjectOwnerId = upload.getDestination().getOwnerId();
        final Integer userId = subjectOwnerId > 0 ? subjectOwnerId : null;
        final Integer groupId = subjectOwnerId < 0 ? Math.abs(subjectOwnerId) : null;
        final int accountId = upload.getAccountId();

        Single<UploadServer> serverSingle;
        if(Objects.nonNull(initialServer)){
            serverSingle = Single.just(initialServer);
        } else {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getWallUploadServer(groupId)
                    .map(s -> s);
        }

        return serverSingle.flatMap(server -> {
            final InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadPhotoToWallRx(server.getUrl(), is[0], listener)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> networker.vkDefault(accountId)
                                .photos()
                                .saveWallPhoto(userId, groupId, dto.photo, dto.server, dto.hash, null, null, null)
                                .flatMap(photos -> {
                                    if(photos.isEmpty()){
                                        return Single.error(new NotFoundException());
                                    }

                                    Photo photo = Dto2Model.transform(photos.get(0));
                                    UploadResult<Photo> result = new UploadResult<>(server, photo);

                                    if(upload.isAutoCommit()){
                                        return commit(attachmentsRepository, upload, photo).andThen(Single.just(result));
                                    } else {
                                        return Single.just(result);
                                    }
                                }));
            } catch (Exception e){
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }

    private Completable commit(IAttachmentsRepository repository, Upload upload, Photo photo){
        int accountId = upload.getAccountId();
        UploadDestination dest = upload.getDestination();

        switch (dest.getMethod()) {
            case Method.PHOTO_TO_COMMENT:
                return repository
                        .attach(accountId, AttachToType.COMMENT, dest.getId(), Collections.singletonList(photo));
            case Method.PHOTO_TO_WALL:
                return repository
                        .attach(accountId, AttachToType.POST, dest.getId(), Collections.singletonList(photo));
        }

        return Completable.error(new UnsupportedOperationException());
    }
}