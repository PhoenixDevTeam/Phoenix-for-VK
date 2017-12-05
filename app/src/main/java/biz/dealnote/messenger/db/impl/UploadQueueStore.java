package biz.dealnote.messenger.db.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.db.UploadSqliteHelper;
import biz.dealnote.messenger.db.column.UploadQueueColumns;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.exception.DatabaseException;
import biz.dealnote.messenger.upload.BaseUploadResponse;
import biz.dealnote.messenger.upload.Method;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.Exestime;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Predicate;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.join;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by ruslan.kolbasa on 27.01.2017.
 * phoenix
 */
class UploadQueueStore extends AbsStore implements IUploadQueueStore {

    private static final int PROGRESS_LOOKUP_DELAY = 500;

    private Observable<Long> timer;
    private PublishSubject<IStatusUpdate> statusUpdatePublishSubject;
    private PublishSubject<List<IQueueUpdate>> queueUpdatesPublishSubject;

    private Map<Integer, Integer> progress;

    @SuppressLint("UseSparseArrays")
    UploadQueueStore(@NonNull AppStores base) {
        super(base);
        this.statusUpdatePublishSubject = PublishSubject.create();
        this.queueUpdatesPublishSubject = PublishSubject.create();
        this.timer = Observable.interval(0L, PROGRESS_LOOKUP_DELAY, TimeUnit.MILLISECONDS);
        this.progress = Collections.synchronizedMap(new HashMap<>(0));
    }

    private UploadSqliteHelper helper() {
        return UploadSqliteHelper.getInstance(getContext());
    }

    @Override
    public Single<List<UploadObject>> getAll(Predicate<UploadObject> filter) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            SQLiteDatabase db = helper().getReadableDatabase();

            Cursor cursor = db.query(UploadQueueColumns.TABLENAME, COLUMNS, null, null, null, null, UploadQueueColumns._ID);

            List<UploadObject> data = new ArrayList<>(safeCountOf(cursor));
            while (cursor.moveToNext()) {
                if (e.isDisposed()) {
                    break;
                }

                UploadObject object = map(cursor);
                if(filter.test(object)){
                    object.setProgress(getProgressById(object.getId()));
                    data.add(object);
                }
            }

            cursor.close();

            e.onSuccess(data);
            Exestime.log("UploadQueueStore.getAll", start, "count: " + data.size() +
                    ", ids: " + Utils.join(data, ",", orig -> String.valueOf(orig.getId())));
        });
    }

    @Override
    public Single<List<UploadObject>> getByDestination(int accountId, int destId, int destOwnerId, @Method int method) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            SQLiteDatabase db = helper().getReadableDatabase();

            String where = UploadQueueColumns.ACCOUNT_ID + " = ? " +
                    " AND " + UploadQueueColumns.DEST_ID1 + " = ? " +
                    " AND " + UploadQueueColumns.DEST_ID2 + " = ? " +
                    " AND " + UploadQueueColumns.METHOD + " = ?";

            String[] args = {String.valueOf(accountId),
                    String.valueOf(destId),
                    String.valueOf(destOwnerId),
                    String.valueOf(method)};

            Cursor cursor = db.query(UploadQueueColumns.TABLENAME, COLUMNS, where, args, null, null, UploadQueueColumns._ID);
            int count = safeCountOf(cursor);

            if(count == 0){
                cursor.close();
                e.onSuccess(Collections.emptyList());
                return;
            }

            if(count == 1){
                cursor.moveToNext();

                UploadObject o = map(cursor);
                o.setProgress(getProgressById(o.getId()));
                cursor.close();
                e.onSuccess(Collections.singletonList(o));
                return;
            }

            List<UploadObject> data = new ArrayList<>(count);
            while (cursor.moveToNext()) {
                if (e.isDisposed()) {
                    break;
                }

                UploadObject object = map(cursor);
                object.setProgress(getProgressById(object.getId()));
                data.add(object);
            }

            cursor.close();

            e.onSuccess(data);
            Exestime.log("UploadQueueStore.getByDestination", start, "count: " + data.size() +
                    ", ids: " + Utils.join(data, ",", orig -> String.valueOf(orig.getId())));
        });
    }

    @Override
    public Single<List<UploadObject>> getByDestination(int accountId, UploadDestination dest) {
        return getByDestination(accountId, dest.getId(), dest.getOwnerId(), dest.getMethod());
    }


    private int getProgressById(int id){
        Integer p = progress.get(id);
        return nonNull(p) ? p : 0;
    }

    private static UploadObject map(Cursor cursor) {
        UploadDestination dest = new UploadDestination(
                cursor.getInt(cursor.getColumnIndex(UploadQueueColumns.DEST_ID1)),
                cursor.getInt(cursor.getColumnIndex(UploadQueueColumns.DEST_ID2)),
                cursor.getInt(cursor.getColumnIndex(UploadQueueColumns.METHOD))
        );

        String rawData = cursor.getString(cursor.getColumnIndex(UploadQueueColumns.DATA));
        UploadIntent.Data data = GSON.fromJson(rawData, UploadIntent.Data.class);

        return new UploadObject(cursor.getInt(cursor.getColumnIndex(UploadQueueColumns.ACCOUNT_ID)))
                .setId(cursor.getInt(cursor.getColumnIndex(UploadQueueColumns._ID)))
                .setDestination(dest)
                .setFileId(data.getFileId())
                .setSize(data.getSize())
                .setFileUri(data.getFileUri())
                .setAutoCommit(data.isAutoCommit())
                .setStatus(cursor.getInt(cursor.getColumnIndex(UploadQueueColumns.STATUS)))
                .setErrorText(cursor.getString(cursor.getColumnIndex(UploadQueueColumns.ERROR_TEXT)));
    }

    private static ContentValues createCv(UploadIntent intent) {
        ContentValues cv = new ContentValues();
        cv.put(UploadQueueColumns.ACCOUNT_ID, intent.getAccountId());
        cv.put(UploadQueueColumns.DEST_ID1, intent.getDestination().getId());
        cv.put(UploadQueueColumns.DEST_ID2, intent.getDestination().getOwnerId());
        cv.put(UploadQueueColumns.METHOD, intent.getDestination().getMethod());
        cv.put(UploadQueueColumns.STATUS, UploadObject.STATUS_QUEUE);

        UploadIntent.Data data = new UploadIntent.Data()
                .setAutoCommit(intent.isAutoCommit())
                .setFileId(intent.getFileId())
                .setFileUri(intent.getFileUri())
                .setSize(intent.getSize());

        cv.put(UploadQueueColumns.DATA, GSON.toJson(data));
        return cv;
    }

    @Override
    public Single<List<UploadObject>> put(List<UploadIntent> intents) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            SQLiteDatabase db = helper().getWritableDatabase();

            int[] ids = new int[intents.size()];

            db.beginTransaction();

            try {
                for (int i = 0; i < intents.size(); i++) {
                    UploadIntent intent = intents.get(i);
                    ContentValues cv = createCv(intent);

                    int id = (int) db.insert(UploadQueueColumns.TABLENAME, null, cv);
                    ids[i] = id;
                }
                db.setTransactionSuccessful();
            } catch (Exception error) {
                e.onError(error);
                return;
            } finally {
                db.endTransaction();
            }

            List<UploadObject> data = new ArrayList<>(intents.size());
            List<IQueueUpdate> updates = new ArrayList<>(intents.size());
            for (int i = 0; i < intents.size(); i++) {
                UploadIntent intent = intents.get(i);

                UploadObject o = new UploadObject(intent.getAccountId())
                        .setDestination(intent.getDestination())
                        .setFileId(intent.getFileId())
                        .setFileUri(intent.getFileUri())
                        .setId(ids[i])
                        .setStatus(UploadObject.STATUS_QUEUE)
                        .setSize(intent.getSize());

                updates.add(new QueueUpdate(o.getId(), true).setObject(o));
                data.add(o);
            }

            queueUpdatesPublishSubject.onNext(updates);

            e.onSuccess(data);

            Exestime.log("UploadQueueStore.put", start, "count: " + intents.size() +
                    ", ids: " + join(data, ",", orig -> String.valueOf(orig.getId())));
        });
    }

    private static class ProgressUpdate implements IProgressUpdate {

        private final int id;

        private final int progress;

        private ProgressUpdate(int id, int progress) {
            this.id = id;
            this.progress = progress;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getProgress() {
            return progress;
        }

        @Override
        public String toString() {
            return "id=" + id + "[" + progress + "%]";
        }
    }

    private List<IProgressUpdate> progressUpdates() {
        if (progress.size() == 0) {
            return Collections.emptyList();
        }

        if (progress.size() == 1) {
            Map.Entry<Integer, Integer> entry = progress.entrySet().iterator().next();
            //for (Map.Entry<Integer, Integer> entry : progress.entrySet()) {
                return Collections.singletonList(new ProgressUpdate(entry.getKey(), entry.getValue()));
            //}
        }

        List<IProgressUpdate> updates = new ArrayList<>(progress.size());
        for (Map.Entry<Integer, Integer> entry : progress.entrySet()) {
            updates.add(new ProgressUpdate(entry.getKey(), entry.getValue()));
        }

        return updates;
    }

    @Override
    public Observable<List<IProgressUpdate>> observeProgress() {
        return timer.flatMap(ignored -> Observable.just(progressUpdates()))
                .filter(updates -> !updates.isEmpty());
    }

    @Override
    public Observable<IStatusUpdate> observeStatusUpdates() {
        return statusUpdatePublishSubject;
    }

    @Override
    public Completable removeWithId(final int id) {
        return removeWithId(id, null);
    }

    @Override
    public Completable removeWithId(final int id, final BaseUploadResponse response) {
        return Completable.create(emitter -> {
            long start = System.currentTimeMillis();

            int count = helper().getWritableDatabase()
                    .delete(UploadQueueColumns.TABLENAME, UploadQueueColumns._ID + " = ?", new String[]{String.valueOf(id)});
            if(count > 0){
                emitter.onComplete();

                progress.remove(id);

                List<IQueueUpdate> updates = Collections.singletonList(new QueueUpdate(id, false).setResponse(response));

                queueUpdatesPublishSubject.onNext(updates);

                Exestime.log("UploadQueueStore.removeById", start, "id: " + id + ", hasResponse: " + nonNull(response));
            } else {
                emitter.onError(new DatabaseException("Record with id " + id + " not found"));
            }
        });
    }

    private static final String[] COLUMNS = {
            UploadQueueColumns._ID,
            UploadQueueColumns.ACCOUNT_ID,
            UploadQueueColumns.DEST_ID1,
            UploadQueueColumns.DEST_ID2,
            UploadQueueColumns.DEST_ID3,
            UploadQueueColumns.METHOD,
            UploadQueueColumns.STATUS,
            UploadQueueColumns.ERROR_TEXT,
            UploadQueueColumns.DATA
    };

    @Override
    public Single<Optional<UploadObject>> findFirstByStatus(int status) {
        return Single.create(emitter -> {
            long start = System.currentTimeMillis();

            Cursor cursor = helper().getReadableDatabase()
                    .query(UploadQueueColumns.TABLENAME, COLUMNS, UploadQueueColumns.STATUS + " = ?",
                            new String[]{String.valueOf(status)}, null, null, UploadQueueColumns._ID + " LIMIT 1");

            UploadObject object = null;
            if(cursor.moveToNext()){
                object = map(cursor);
            }

            cursor.close();
            emitter.onSuccess(Optional.wrap(object));
            Exestime.log("UploadQueueStore.findFirst", start, "object: " + object);
        });
    }

    private static class QueueUpdate implements IQueueUpdate {

        private final int id;

        private final boolean add;

        private UploadObject object;

        private BaseUploadResponse response;

        private QueueUpdate(int id, boolean add) {
            this.id = id;
            this.add = add;
        }

        public QueueUpdate setObject(UploadObject object) {
            this.object = object;
            return this;
        }

        @Override
        public int getId() {
            return id;
        }

        @Nullable
        @Override
        public UploadObject object() {
            return object;
        }

        @Override
        public boolean isAdding() {
            return add;
        }

        @Nullable
        @Override
        public BaseUploadResponse response() {
            return response;
        }

        public QueueUpdate setResponse(BaseUploadResponse response) {
            this.response = response;
            return this;
        }

        @Override
        public String toString() {
            return "QueueUpdate{" + "id=" + id + ", add=" + add + '}';
        }
    }

    @Override
    public Completable changeStatus(int id, int status) {
        return Completable.create(emitter -> {
            long start = System.currentTimeMillis();

            ContentValues cv = new ContentValues();
            cv.put(UploadQueueColumns.STATUS, status);

            int count = helper().getWritableDatabase().update(UploadQueueColumns.TABLENAME,
                    cv, UploadQueueColumns._ID + " = ?", new String[]{String.valueOf(id)});
            if (count > 0) {
                emitter.onComplete();

                if(status != UploadObject.STATUS_UPLOADING){
                    progress.remove(id);
                }

                IStatusUpdate statusUpdate = new StatusUpdate(id, status);
                statusUpdatePublishSubject.onNext(statusUpdate);

                Exestime.log("UploadQueueStore.changeStatus", start, "id: " + id + ", status:" + statusOf(status));
            } else {
                emitter.onError(new DatabaseException("Record with id " + id + " not found"));
            }
        });
    }

    private static String statusOf(int value){
        switch (value){
            case UploadObject.STATUS_CANCELLING:
                return "CANCELLING";
            case UploadObject.STATUS_ERROR:
                return "ERROR";
            case UploadObject.STATUS_QUEUE:
                return "QUEUE";
            case UploadObject.STATUS_UPLOADING:
                return "UPLOADING";
            default:
                return "UNKNOWN";
        }
    }

    private static class StatusUpdate implements IStatusUpdate {

        private final int id;

        private final int status;

        private StatusUpdate(int id, int status) {
            this.id = id;
            this.status = status;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getStatus() {
            return status;
        }
    }

    @Override
    public Observable<List<IQueueUpdate>> observeQueue() {
        return queueUpdatesPublishSubject;
    }

    @Override
    public void changeProgress(int id, int progressValue) {
        progress.put(id, progressValue);
    }
}
