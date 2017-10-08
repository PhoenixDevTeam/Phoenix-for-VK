package biz.dealnote.messenger.upload.task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.WeakPercentageListener;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.api.model.upload.UploadDocDto;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.db.model.entity.DocumentEntity;
import biz.dealnote.messenger.domain.mappers.Dto2Entity;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.upload.BaseUploadResponse;
import biz.dealnote.messenger.upload.UploadCallback;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.IOUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import retrofit2.Call;

import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

public class DocumentUploadTask extends AbstractUploadTask<DocumentUploadTask.Response> {

    private Integer mOwnerId;

    public DocumentUploadTask(@NonNull Context context, @NonNull UploadCallback callback,
                              @NonNull UploadObject uploadObject, @Nullable UploadServer initialServer) {
        super(context, callback, uploadObject, initialServer);
        this.mOwnerId = uploadObject.getDestination().getOwnerId();
    }

    private static String findFileName(Context context, Uri uri) {
        String fileName = uri.getLastPathSegment();
        try {
            String scheme = uri.getScheme();
            if (scheme.equals("file")) {
                fileName = uri.getLastPathSegment();
            } else if (scheme.equals("content")) {
                String[] proj = {MediaStore.Images.Media.TITLE};

                Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }

                if (cursor != null) {
                    cursor.close();
                }
            }

        } catch (Exception ignored) {

        }

        return fileName;
    }

    @Override
    protected Response doUpload(@Nullable UploadServer server, @NonNull UploadObject uploadObject) throws CancelException {
        int accountId = uploadObject.getAccountId();
        Integer targetGroupId = mOwnerId == null || mOwnerId >= 0 ? null : mOwnerId;
        Response result = new Response();

        InputStream is = null;

        try {
            Uri uri = uploadObject.getFileUri();

            File file = new File(uri.getPath());
            if(file.isFile()){
                is = new FileInputStream(file);
            } else {
                is = getContext().getContentResolver().openInputStream(uri);
            }

            String filename = findFileName(getContext(), uri);

            if (server == null) {
                server = Apis.get()
                        .vkDefault(accountId)
                        .docs()
                        .getUploadServer(targetGroupId, null)
                        .blockingGet();

                result.setServer(server);
            }

            assertCancel(this);

            String serverUrl = server.getUrl();

            Call<UploadDocDto> call = Apis.get()
                    .uploads()
                    .uploadDocument(serverUrl, filename, is, new WeakPercentageListener(this));

            registerCall(call);

            UploadDocDto entity;

            try {
                entity = call.execute().body();
            } catch (Exception e) {
                result.setError(e);
                return result;
            } finally {
                unregisterCall(call);
            }

            assertCancel(this);

            List<VkApiDoc> dtos = Apis.get()
                    .vkDefault(accountId)
                    .docs()
                    .save(entity.file, filename, null)
                    .blockingGet();

            assertCancel(this);

            saveDocumentsToDb(uploadObject.getAccountId(), dtos);

            List<Document> documents = new ArrayList<>(safeCountOf(dtos));
            if (Objects.nonNull(dtos)) {
                for (VkApiDoc dto : dtos) {
                    documents.add(Dto2Model.transform(dto));
                }
            }

            result.documents = documents;
        } catch (Exception e) {
            e.printStackTrace();
            result.setError(e);
        } finally {
            IOUtils.closeStreamQuietly(is);
        }

        return result;
    }

    private void saveDocumentsToDb(int aid, List<VkApiDoc> documents) {
        if (Utils.safeIsEmpty(documents)) {
            return;
        }

        for (VkApiDoc dto : documents) {
            DocumentEntity entity = Dto2Entity.buildDocumentDbo(dto);

            Stores.getInstance()
                    .docs()
                    .store(aid, dto.ownerId, Collections.singletonList(entity), false)
                    .blockingAwait();
        }
    }

    public static class Response extends BaseUploadResponse {

        public List<Document> documents;

        @Override
        public boolean isSuccess() {
            return nonEmpty(documents);
        }
    }
}