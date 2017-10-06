package biz.dealnote.messenger.service.operations.photo;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.PhotosColumns;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;
import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.service.operations.AbsApiOperation;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Utils.safeCountOf;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public class GetPhotoByIdOperation extends AbsApiOperation {

    public static final String EXTRA_STORE_TO_DB = "store_to_db";

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        ArrayList<AccessIdPair> ids = request.getParcelableArrayList(Extra.IDS);
        boolean storeToDb = request.getBoolean(EXTRA_STORE_TO_DB);

        Bundle bundle = new Bundle();
        if (safeIsEmpty(ids)) {
            return buildSimpleSuccessResult(false);
        }

        List<biz.dealnote.messenger.api.model.AccessIdPair> dtoPairs = new ArrayList<>(ids.size());
        for(AccessIdPair pair : ids){
            dtoPairs.add(new biz.dealnote.messenger.api.model.AccessIdPair(pair.getId(),
                    pair.getOwnerId(), pair.getAccessKey()));
        }

        List<VKApiPhoto> dtos = Apis.get()
                .vkDefault(accountId)
                .photos()
                .getById(dtoPairs)
                .blockingGet();

        //List<VKApiPhoto> result = api.getPhotosById(line, true, false);

        ArrayList<Photo> photos = new ArrayList<>(safeCountOf(dtos));

        for (VKApiPhoto resultPhoto : dtos) {
            photos.add(Dto2Model.transform(resultPhoto));
        }

        bundle.putParcelableArrayList(Extra.PHOTOS, photos);
        bundle.putBoolean(Extra.SUCCESS, true);

        if (storeToDb) {
            save(context, accountId, dtos);
        }

        Logger.d("GetPhotoByIdOperation", "photos: " + photos);
        return bundle;
    }

    private void save(Context context, int aid, List<VKApiPhoto> photos) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (VKApiPhoto photo : photos) {
            Integer exist = findPhoto(context, aid, photo.id, photo.owner_id);

            if (exist != null) {
                operations.add(ContentProviderOperation
                        .newUpdate(MessengerContentProvider.getPhotosContentUriFor(aid))
                        .withSelection(PhotosColumns._ID + " = ?", new String[]{exist.toString()})
                        .withValues(PhotosColumns.getCV(photo))
                        .build());
            } else {
                operations.add(ContentProviderOperation
                        .newInsert(MessengerContentProvider.getPhotosContentUriFor(aid))
                        .withValues(PhotosColumns.getCV(photo))
                        .build());
            }
        }

        context.getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
    }

    private Integer findPhoto(Context context, int aid, int id, int ownerId) {
        String[] columns = {PhotosColumns._ID};
        Cursor cursor = context.getContentResolver().query(MessengerContentProvider.getPhotosContentUriFor(aid), columns,
                PhotosColumns.PHOTO_ID + " = ? AND " + PhotosColumns.OWNER_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(ownerId)}, null);
        if (cursor == null) {
            return null;
        }

        Integer dbid = null;
        if (cursor.moveToNext()) {
            dbid = cursor.getInt(cursor.getColumnIndex(PhotosColumns._ID));
        }

        cursor.close();
        return dbid;
    }
}
