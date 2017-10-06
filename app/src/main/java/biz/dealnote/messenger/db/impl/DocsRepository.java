package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.DocColumns;
import biz.dealnote.messenger.db.interfaces.IDocsRepository;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;
import biz.dealnote.messenger.model.DocFilter;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.criteria.DocsCriteria;
import biz.dealnote.messenger.util.Exestime;
import io.reactivex.Maybe;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
class DocsRepository extends AbsRepository implements IDocsRepository {

    DocsRepository(@NonNull AppRepositories base) {
        super(base);
    }

    @Override
    public Single<List<Document>> get(@NonNull DocsCriteria criteria) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            Uri uri = MessengerContentProvider.getDocsContentUriFor(criteria.getAccountId());

            String where;
            String[] args;

            Integer filter = criteria.getFilter();

            if (nonNull(filter) && filter != DocFilter.Type.ALL) {
                where = DocColumns.OWNER_ID + " = ? AND " + DocColumns.TYPE + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId()), String.valueOf(filter)};
            } else {
                where = DocColumns.OWNER_ID + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId())};
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);
            List<Document> data = mapAll(e::isDisposed, cursor, cursor1 -> map(cursor), true);

            e.onSuccess(data);

            Exestime.log("DocsRepository.get", start, "count: " + data.size());
        });
    }

    @Override
    public Maybe<DatabaseIdRange> store(int accountId, int ownerId, List<VkApiDoc> dtos, boolean clearBeforeInsert) {
        return Maybe.create(e -> {
            long start = System.currentTimeMillis();

            Uri uri = MessengerContentProvider.getDocsContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            if (clearBeforeInsert) {
                operations.add(ContentProviderOperation.newDelete(uri)
                        .withSelection(DocColumns.OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)})
                        .build());
            }

            for (VkApiDoc dto : dtos) {
                ContentValues cv = new ContentValues();
                cv.put(DocColumns.DOC_ID, dto.id);
                cv.put(DocColumns.OWNER_ID, dto.ownerId);
                cv.put(DocColumns.TITLE, dto.title);
                cv.put(DocColumns.SIZE, dto.size);
                cv.put(DocColumns.EXT, dto.ext);
                cv.put(DocColumns.URL, dto.url);
                cv.put(DocColumns.DATE, dto.date);
                cv.put(DocColumns.TYPE, dto.type);
                cv.put(DocColumns.ACCESS_KEY, dto.accessKey);
                cv.put(DocColumns.PREVIEW, isNull(dto.preview) ? null : GSON.toJson(dto.preview));

                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            ContentProviderResult[] results = getContentResolver()
                    .applyBatch(MessengerContentProvider.AUTHORITY, operations);

            DatabaseIdRange range = DatabaseIdRange.createFromContentProviderResults(results);
            if(nonNull(range)){
                e.onSuccess(range);
            }

            e.onComplete();

            Exestime.log("DocsRepository.store", start, "count: " + dtos.size());
        });
    }

    private static Document map(Cursor cursor) {
        Document document = new Document(cursor.getInt(cursor.getColumnIndex(DocColumns.DOC_ID)), cursor.getInt(cursor.getColumnIndex(DocColumns.OWNER_ID)))
                .setTitle(cursor.getString(cursor.getColumnIndex(DocColumns.TITLE)))
                .setSize(cursor.getLong(cursor.getColumnIndex(DocColumns.SIZE)))
                .setExt(cursor.getString(cursor.getColumnIndex(DocColumns.EXT)))
                .setUrl(cursor.getString(cursor.getColumnIndex(DocColumns.URL)))
                .setType(cursor.getInt(cursor.getColumnIndex(DocColumns.TYPE)))
                .setDate(cursor.getLong(cursor.getColumnIndex(DocColumns.DATE)))
                .setAccessKey(cursor.getString(cursor.getColumnIndex(DocColumns.ACCESS_KEY)));

        String previewJson = cursor.getString(cursor.getColumnIndex(DocColumns.PREVIEW));

        if (!safeIsEmpty(previewJson)) {
            VkApiDoc.Preview preview = GSON.fromJson(previewJson, VkApiDoc.Preview.class);

            if (nonNull(preview)) {
                if (nonNull(preview.video)) {
                    document.setVideoPreview(Dto2Model.transform(preview.video));
                }

                if (nonNull(preview.photo) && nonNull(preview.photo.sizes)) {
                    document.setPhotoPreview(Dto2Model.transform(preview.photo.sizes));
                }

                if (nonNull(preview.graffiti)) {
                    document.setGraffiti(Dto2Model.transform(preview.graffiti));
                }
            }
        }

        return document;
    }
}
