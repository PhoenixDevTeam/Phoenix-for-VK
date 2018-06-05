package biz.dealnote.messenger.upload.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.PercentageListener;
import biz.dealnote.messenger.api.WeakPercentageListener;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToMessageDto;
import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.upload.BaseUploadResponse;
import biz.dealnote.messenger.upload.UploadCallback;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.IOUtils;
import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.Call;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;
import static biz.dealnote.messenger.util.Utils.safelyClose;

public class PhotoMessageUploadTask extends AbstractUploadTask<PhotoMessageUploadTask.Response> {

    public PhotoMessageUploadTask(@NonNull Context context, @NonNull UploadCallback callback,
                                  @NonNull UploadObject uploadObject, @Nullable UploadServer server) {
        super(context, callback, uploadObject, server);
    }

    @SuppressWarnings("unused")
    private static Single<Response> doUploadRx(@NonNull Context context, @NonNull INetworker networker, @Nullable UploadServer initialServer,
                                        @NonNull UploadObject upload, @Nullable PercentageListener listener) {
        final int accountId = upload.getAccountId();
        final int messageId = upload.getDestination().getId();

        Single<UploadServer> serverSingle;
        if (nonNull(initialServer)) {
            serverSingle = Single.just(initialServer);
        } else {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getMessagesUploadServer().map(s -> s);
        }

        return serverSingle.flatMap(server -> {
            final InputStream[] is = new InputStream[1];

            try {
                is[0] = openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadPhotoToMessageRx(server.getUrl(), is[0], listener)
                        .doFinally(() -> safelyClose(is[0]))
                        .flatMap(dto -> networker.vkDefault(accountId)
                                .photos()
                                .saveMessagesPhoto(dto.server, dto.photo, dto.hash)
                                .flatMap(photos -> {
                                    if(photos.isEmpty()){
                                        return Single.error(new NotFoundException());
                                    }

                                    Photo photo = Dto2Model.transform(photos.get(0));

                                    Response response = new Response();
                                    response.setServer(server);
                                    response.setPhoto(photo);

                                    if(upload.isAutoCommit()){
                                        return attachIntoDatabaseRx(accountId, messageId, photo)
                                                .andThen(Single.just(response));
                                    } else {
                                        return Single.just(response);
                                    }
                                }));
            } catch (Exception e){
                return Single.error(e);
            } finally {
                safelyClose(is[0]);
            }
        });
    }

    private static Completable attachIntoDatabaseRx(int accountId, int messageId, Photo photo){
        return Injection.provideAttachmentsRepository()
                .attach(accountId, AttachToType.MESSAGE, messageId, Collections.singletonList(photo))
                .andThen(Stores.getInstance().messages().notifyMessageHasAttachments(accountId, messageId));
    }

    @Override
    protected Response doUpload(@Nullable UploadServer server, @NonNull UploadObject uploadObject) throws CancelException {
        int accountId = uploadObject.getAccountId();

        Response result = new Response();

        InputStream is = null;

        try {
            is = openStream(getContext(), uploadObject.getFileUri(), uploadObject.getSize());

            assertCancel(this);

            if (server == null) {
                server = Apis.get()
                        .vkDefault(accountId)
                        .photos()
                        .getMessagesUploadServer()
                        .blockingGet();

                result.setServer(server);
            }

            assertCancel(this);

            String serverUrl = server.getUrl();

            Call<UploadPhotoToMessageDto> call = Apis.get()
                    .uploads()
                    .uploadPhotoToMessage(serverUrl, is, new WeakPercentageListener(this));

            registerCall(call);

            UploadPhotoToMessageDto entity;

            try {
                entity = call.execute().body();
            } catch (Exception e) {
                result.setError(e);
                return result;
            } finally {
                unregisterCall(call);
            }

            assertCancel(this);

            List<VKApiPhoto> photos = Apis.get()
                    .vkDefault(accountId)
                    .photos()
                    .saveMessagesPhoto(entity.server, entity.photo, entity.hash)
                    .blockingGet();

            assertCancel(this);

            if (safeCountOf(photos) == 1) {
                VKApiPhoto dto = photos.get(0);

                if (uploadObject.isAutoCommit()) {
                    attachIntoDatabase(dto);
                }

                result.photo = Dto2Model.transform(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setError(e);
        } finally {
            IOUtils.closeStreamQuietly(is);
        }

        return result;
    }

    private void attachIntoDatabase(@NonNull VKApiPhoto dto) {
        int aid = uploadObject.getAccountId();
        int mid = uploadObject.getDestination().getId();
        Photo photo = Dto2Model.transform(dto);

        Injection.provideAttachmentsRepository()
                .attach(aid, AttachToType.MESSAGE, mid, Collections.singletonList(photo))
                .andThen(Stores.getInstance().messages().notifyMessageHasAttachments(aid, mid))
                .blockingAwait();
    }

    public static class Response extends BaseUploadResponse {

        public Photo photo;

        public void setPhoto(Photo photo) {
            this.photo = photo;
        }

        @Override
        public boolean isSuccess() {
            return nonNull(photo);
        }
    }
}