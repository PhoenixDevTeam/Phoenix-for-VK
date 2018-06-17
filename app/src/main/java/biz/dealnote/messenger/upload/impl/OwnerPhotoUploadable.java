package biz.dealnote.messenger.upload.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;

import biz.dealnote.messenger.api.PercentagePublisher;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.upload.IUploadable;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.upload.UploadResult;
import biz.dealnote.messenger.upload.UploadUtils;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Utils.safelyClose;

public class OwnerPhotoUploadable implements IUploadable<Post> {

    private final Context context;
    private final INetworker networker;
    private final IWalls walls;

    public OwnerPhotoUploadable(Context context, INetworker networker, IWalls walls) {
        this.context = context;
        this.networker = networker;
        this.walls = walls;
    }

    @Override
    public Single<UploadResult<Post>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        final int accountId = upload.getAccountId();
        final int ownerId = upload.getDestination().getOwnerId();

        Single<UploadServer> serverSingle;
        if (initialServer == null) {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getOwnerPhotoUploadServer(ownerId)
                    .map(s -> s);
        } else {
            serverSingle = Single.just(initialServer);
        }

        return serverSingle.flatMap(server -> {
            final InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadOwnerPhotoRx(server.getUrl(), is[0], listener)
                        .doFinally(() -> safelyClose(is[0]))
                        .flatMap(dto -> networker.vkDefault(accountId)
                                .photos()
                                .saveOwnerPhoto(dto.server, dto.hash, dto.photo)
                                .flatMap(response -> {
                                    if (response.postId == 0) {
                                        return Single.error(new NotFoundException("Post id=0"));
                                    }

                                    return walls.getById(accountId, ownerId, response.postId)
                                            .map(post -> new UploadResult<>(server, post));
                                }));
            } catch (Exception e) {
                return Single.error(e);
            } finally {
                safelyClose(is[0]);
            }
        });
    }
}