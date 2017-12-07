package biz.dealnote.messenger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import biz.dealnote.messenger.crypt.AesKeyPair;
import biz.dealnote.messenger.crypt.ver.Version;
import biz.dealnote.messenger.db.column.AttachmentsColumns;
import biz.dealnote.messenger.db.column.CommentsAttachmentsColumns;
import biz.dealnote.messenger.db.column.CommentsColumns;
import biz.dealnote.messenger.db.column.CountriesColumns;
import biz.dealnote.messenger.db.column.DialogsColumns;
import biz.dealnote.messenger.db.column.DocColumns;
import biz.dealnote.messenger.db.column.FaveLinksColumns;
import biz.dealnote.messenger.db.column.FavePhotosColumns;
import biz.dealnote.messenger.db.column.FavePostsColumns;
import biz.dealnote.messenger.db.column.FaveUsersColumns;
import biz.dealnote.messenger.db.column.FaveVideosColumns;
import biz.dealnote.messenger.db.column.FeedListsColumns;
import biz.dealnote.messenger.db.column.FriendListsColumns;
import biz.dealnote.messenger.db.column.GroupColumns;
import biz.dealnote.messenger.db.column.GroupContactsColumns;
import biz.dealnote.messenger.db.column.GroupLinksColumns;
import biz.dealnote.messenger.db.column.GroupsDetColumns;
import biz.dealnote.messenger.db.column.KeyColumns;
import biz.dealnote.messenger.db.column.MessageColumns;
import biz.dealnote.messenger.db.column.NewsColumns;
import biz.dealnote.messenger.db.column.NotificationColumns;
import biz.dealnote.messenger.db.column.PhotoAlbumsColumns;
import biz.dealnote.messenger.db.column.PhotosColumns;
import biz.dealnote.messenger.db.column.PostAttachmentsColumns;
import biz.dealnote.messenger.db.column.PostsColumns;
import biz.dealnote.messenger.db.column.RelationshipColumns;
import biz.dealnote.messenger.db.column.StikerSetColumns;
import biz.dealnote.messenger.db.column.TopicsColumns;
import biz.dealnote.messenger.db.column.UserCareerColumns;
import biz.dealnote.messenger.db.column.UserColumns;
import biz.dealnote.messenger.db.column.UsersDetColumns;
import biz.dealnote.messenger.db.column.VideoAlbumsColumns;
import biz.dealnote.messenger.db.column.VideoColumns;

public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = "DBHelper";

    private static final int DATABASE_VERSION = 155;

    private static volatile Map<Integer, DBHelper> dbHelperMap = new ConcurrentHashMap<>();

    @NonNull
    public synchronized static DBHelper getInstance(Context context, int aid) {
        DBHelper helper = dbHelperMap.get(aid);
        if (helper == null) {
            helper = new DBHelper(context, aid);
            dbHelperMap.put(aid, helper);
        }

        return helper;
    }

    public static void removeDatabaseFor(Context context, int aid) {
        dbHelperMap.remove(aid);
        context.deleteDatabase(DBHelper.getDatabaseFileName(aid));
    }

    private DBHelper(Context context, int aid) {
        super(context, getDatabaseFileName(aid), null, DATABASE_VERSION);
    }

    private static String getDatabaseFileName(int aid) {
        return "vksm" + aid + ".sqlite";
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old, int i2) {
        if (old < DATABASE_VERSION) {
            dropAllTables(db);
            onCreate(db);
        }

        if (old < 114) {
            addKeyVersioning(db);
        }
    }

    private void addKeyVersioning(SQLiteDatabase db) {
        Cursor cursor = db.query(KeyColumns.TABLENAME, new String[]{
                KeyColumns.PEER_ID,
                KeyColumns.SESSION_ID,
                KeyColumns.DATE,
                KeyColumns.START_SESSION_MESSAGE_ID,
                KeyColumns.END_SESSION_MESSAGE_ID,
                KeyColumns.OUT_KEY,
                KeyColumns.IN_KEY}, null, null, null, null, null);

        ArrayList<AesKeyPair> pairs = new ArrayList<>();
        while (cursor.moveToNext()) {
            AesKeyPair pair = new AesKeyPair()
                    .setPeerId(cursor.getInt(0))
                    .setSessionId(cursor.getLong(1))
                    .setDate(cursor.getLong(2))
                    .setStartMessageId(cursor.getInt(3))
                    .setEndMessageId(cursor.getInt(4))
                    .setMyAesKey(cursor.getString(5))
                    .setHisAesKey(cursor.getString(6))
                    .setVersion(Version.V1);
            pairs.add(pair);
        }

        cursor.close();

        db.execSQL("DROP TABLE IF EXISTS " + KeyColumns.TABLENAME);

        createKeysTableIfNotExist(db);

        db.beginTransaction();

        for (AesKeyPair pair : pairs) {
            ContentValues cv = new ContentValues();
            cv.put(KeyColumns.VERSION, pair.getVersion());
            cv.put(KeyColumns.PEER_ID, pair.getPeerId());
            cv.put(KeyColumns.SESSION_ID, pair.getSessionId());
            cv.put(KeyColumns.DATE, pair.getDate());
            cv.put(KeyColumns.START_SESSION_MESSAGE_ID, pair.getStartMessageId());
            cv.put(KeyColumns.END_SESSION_MESSAGE_ID, pair.getEndMessageId());
            cv.put(KeyColumns.OUT_KEY, pair.getMyAesKey());
            cv.put(KeyColumns.IN_KEY, pair.getHisAesKey());
            db.insert(KeyColumns.TABLENAME, null, cv);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);

        createMessagesTable(db);
        createZeroMessageProtectionTriggers(db);

        createAttachmentsTable(db);
        createDialogTable(db);
        createPhotoTable(db);
        createDocsTable(db);
        createVideosTable(db);
        createPostAttachmentsTable(db);
        createPostsTable(db);
        createGroupsTable(db);
        createRelativeshipTable(db);
        createCommentsTable(db);
        createCommentsAttachmentsTable(db);
        createPhotoAlbumsTable(db);
        //createLinksTable(db);
        //createPollTable(db);
        createNewsTable(db);
        //createNewsAttachmentsTable(db);
        createGroupsDetTable(db);
        createVideoAlbumsTable(db);
        createTopicsTable(db);
        createNotoficationsTable(db);
        createUserDetTable(db);
        createUserCareerTable(db);
        createStickerSetTable(db);
        createFavePhotosTable(db);
        createFaveVideosTable(db);
        createFaveUsersTable(db);
        createFaveLinksTable(db);
        createFavePostsTable(db);
        createCountriesTable(db);
        createFeedListsTable(db);
        createFriendListsTable(db);

        //Triggers.createInsertPeerWithUserTrigger(db);
        //Triggers.createUpdateDialogWithUserTitleTrigger(db);
        //Triggers.createUpdatedGroupToPeerTrigger(db);
        //Triggers.createInsertedGroupToPeerTrigger(db);

        //createDeleteWallCopyHistoryTrigger(db);
        //createDeleteFeedCopyHistoryTrigger(db);

        // for test
        createKeysTableIfNotExist(db);
    }

    private void createKeysTableIfNotExist(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS [" + KeyColumns.TABLENAME + "] (\n" +
                "  [" + KeyColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + KeyColumns.VERSION + "] INTEGER, " +
                "  [" + KeyColumns.PEER_ID + "] INTEGER, " +
                "  [" + KeyColumns.SESSION_ID + "] INTEGER, " +
                "  [" + KeyColumns.DATE + "] BIGINT, " +
                "  [" + KeyColumns.START_SESSION_MESSAGE_ID + "] INTEGER, " +
                "  [" + KeyColumns.END_SESSION_MESSAGE_ID + "] INTEGER, " +
                "  [" + KeyColumns.OUT_KEY + "] TEXT, " +
                "  [" + KeyColumns.IN_KEY + "] TEXT," +
                "  CONSTRAINT [] UNIQUE ([" + KeyColumns.SESSION_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void dropAllTables(SQLiteDatabase db) {
        db.beginTransaction();

        // сначала удаляем триггеры, потому что они зависят от таблиц
        db.execSQL("DROP TRIGGER IF EXISTS t_update_user_to_peer");
        db.execSQL("DROP TRIGGER IF EXISTS t_update_group_to_peer");
        db.execSQL("DROP TRIGGER IF EXISTS t_insert_user_to_peer");
        db.execSQL("DROP TRIGGER IF EXISTS t_update_group_to_peer");
        db.execSQL("DROP TRIGGER IF EXISTS t_delete_wall_copy_history");
        db.execSQL("DROP TRIGGER IF EXISTS t_delete_feed_copy_history");

        db.execSQL("DROP TABLE IF EXISTS " + AttachmentsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentsAttachmentsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + DialogsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + DocColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + GroupColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + GroupContactsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + GroupLinksColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + GroupsDetColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS links");

        //messages
        db.execSQL("DROP TRIGGER IF EXISTS zero_msg_upd");
        db.execSQL("DROP TRIGGER IF EXISTS zero_msg_del");
        db.execSQL("DROP TABLE IF EXISTS " + MessageColumns.TABLENAME);

        db.execSQL("DROP TABLE IF EXISTS news_attachments");
        db.execSQL("DROP TABLE IF EXISTS " + NewsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + PhotoAlbumsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + PhotosColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS polls");
        db.execSQL("DROP TABLE IF EXISTS " + PostAttachmentsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + PostsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + RelationshipColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + VideoAlbumsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + VideoColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + TopicsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + NotificationColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + UsersDetColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserCareerColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + StikerSetColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavePhotosColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + FaveVideosColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + FaveUsersColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + FaveLinksColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavePostsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + "peers");
        db.execSQL("DROP TABLE IF EXISTS " + CountriesColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + FeedListsColumns.TABLENAME);
        db.execSQL("DROP TABLE IF EXISTS " + FriendListsColumns.TABLENAME);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void createZeroMessageProtectionTriggers(SQLiteDatabase db) {
        String sqlUpdate = "CREATE TRIGGER zero_msg_upd BEFORE UPDATE ON " + MessageColumns.TABLENAME + " FOR EACH ROW " +
                "WHEN OLD." + MessageColumns._ID + " = 0 BEGIN " +
                "   SELECT RAISE(ABORT, 'Cannot update record with _id=0');" +
                "END;";

        String sqlDelete = "CREATE TRIGGER zero_msg_del BEFORE DELETE ON " + MessageColumns.TABLENAME + " FOR EACH ROW " +
                "WHEN OLD." + MessageColumns._ID + " = 0 BEGIN " +
                "   SELECT RAISE(ABORT, 'Cannot delete record with _id=0');" +
                "END;";

        db.execSQL(sqlUpdate);
        db.execSQL(sqlDelete);
    }

    private void createStickerSetTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + StikerSetColumns.TABLENAME + "] (\n" +
                " [" + StikerSetColumns._ID + "] INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                " [" + StikerSetColumns.TITLE + "] TEXT, " +
                " [" + StikerSetColumns.PHOTO_35 + "] TEXT, " +
                " [" + StikerSetColumns.PHOTO_70 + "] TEXT, " +
                " [" + StikerSetColumns.PHOTO_140 + "] TEXT, " +
                //" [" + StikerSetColumns.PHOTO_296 + "] VARCHAR(2048), " +
                //" [" + StikerSetColumns.PHOTO_592 + "] VARCHAR(2048), " +
                //" [" + StikerSetColumns.BACKGROUND + "] VARCHAR(2048), " +
                //" [" + StikerSetColumns.DESCRIPTION + "] VARCHAR(2048), " +
                " [" + StikerSetColumns.PURCHASED + "] BOOLEAN, " +
                " [" + StikerSetColumns.PROMOTED + "] BOOLEAN, " +
                " [" + StikerSetColumns.ACTIVE + "] BOOLEAN, " +
                //" [" + StikerSetColumns.TYPE + "] VARCHAR(2048), " +
                //" [" + StikerSetColumns.BASE_URL + "] VARCHAR(2048), " +
                //" [" + StikerSetColumns.AUTHOR + "] VARCHAR(2048), " +
                //" [" + StikerSetColumns.FREE + "] BOOLEAN, " +
                //" [" + StikerSetColumns.CAN_PURCHASE + "] BOOLEAN, " +
                //" [" + StikerSetColumns.PAYMENT_TYPE + "] VARCHAR(2048), " +
                //" [" + StikerSetColumns.STICKERS_BASE_URL + "] VARCHAR(2048), " +
                " [" + StikerSetColumns.STICKERS_IDS + "] TEXT, " +
                " CONSTRAINT [] PRIMARY KEY([" + StikerSetColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createCountriesTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + CountriesColumns.TABLENAME + "] (\n" +
                " [" + CountriesColumns._ID + "] INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                " [" + CountriesColumns.NAME + "] TEXT, " +
                " CONSTRAINT [] PRIMARY KEY([" + CountriesColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createPhotoTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + PhotosColumns.TABLENAME + "] (\n" +
                "  [" + PhotosColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + PhotosColumns.PHOTO_ID + "] INTEGER, " +
                "  [" + PhotosColumns.ALBUM_ID + "] INTEGER, " +
                "  [" + PhotosColumns.OWNER_ID + "] INTEGER, " +
                "  [" + PhotosColumns.WIDTH + "] INTEGER, " +
                "  [" + PhotosColumns.HEIGHT + "] INTEGER, " +
                "  [" + PhotosColumns.TEXT + "] TEXT, " +
                "  [" + PhotosColumns.DATE + "] INTEGER, " +
                "  [" + PhotosColumns.SIZES + "] TEXT, " +
                "  [" + PhotosColumns.USER_LIKES + "] BOOLEAN, " +
                "  [" + PhotosColumns.CAN_COMMENT + "] BOOLEAN, " +
                "  [" + PhotosColumns.LIKES + "] INTEGER, " +
                "  [" + PhotosColumns.COMMENTS + "] INTEGER, " +
                "  [" + PhotosColumns.TAGS + "] INTEGER, " +
                "  [" + PhotosColumns.ACCESS_KEY + "] TEXT, " +
                "  [" + PhotosColumns.DELETED + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + PhotosColumns.PHOTO_ID + "], [" + PhotosColumns.OWNER_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createAttachmentsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + AttachmentsColumns.TABLENAME + "] (\n" +
                " [" + AttachmentsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + AttachmentsColumns.MESSAGE_ID + "] INTEGER, " +
                " [" + AttachmentsColumns.TYPE + "] INTEGER, " +
                " [" + AttachmentsColumns.DATA + "] TEXT, " +
                //" [" + AttachmentsColumns.ATTACHMENT_ID + "] INTEGER, " +
                //" [" + AttachmentsColumns.ATTACHMENT_OWNER_ID + "] INTEGER, " +
                //" CONSTRAINT [] UNIQUE ([" + AttachmentsColumns.MESSAGE_ID + "], [" + AttachmentsColumns.ATTACHMENT_ID + "], [" + AttachmentsColumns.ATTACHMENT_OWNER_ID + "], [" + AttachmentsColumns.TYPE + "]) ON CONFLICT REPLACE," +
                " FOREIGN KEY([" + AttachmentsColumns.MESSAGE_ID + "]) " +
                " REFERENCES " + MessageColumns.TABLENAME + "([" + MessageColumns._ID + "]) ON DELETE CASCADE ON UPDATE CASCADE);";
        db.execSQL(sql);
    }

    /**
     * Создание таблицы комментариев
     *
     * @param db БД
     */
    private void createCommentsTable(SQLiteDatabase db) {
        String create = "CREATE TABLE [" + CommentsColumns.TABLENAME + "] (\n" +
                " [" + CommentsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + CommentsColumns.COMMENT_ID + "] INTEGER, " +
                " [" + CommentsColumns.FROM_ID + "] INTEGER, " +
                " [" + CommentsColumns.DATE + "] TEXT, " +
                " [" + CommentsColumns.TEXT + "] TEXT, " +
                " [" + CommentsColumns.REPLY_TO_USER + "] INTEGER, " +
                " [" + CommentsColumns.REPLY_TO_COMMENT + "] INTEGER, " +
                " [" + CommentsColumns.LIKES + "] INTEGER, " +
                " [" + CommentsColumns.USER_LIKES + "] BOOLEAN, " +
                " [" + CommentsColumns.CAN_LIKE + "] BOOLEAN, " +
                " [" + CommentsColumns.CAN_EDIT + "] BOOLEAN, " +
                " [" + CommentsColumns.ATTACHMENTS_COUNT + "] INTEGER, " +
                " [" + CommentsColumns.DELETED + "] BOOLEAN, " +
                " [" + CommentsColumns.SOURCE_ID + "] INTEGER, " +
                " [" + CommentsColumns.SOURCE_OWNER_ID + "] INTEGER, " +
                " [" + CommentsColumns.SOURCE_TYPE + "] INTEGER, " +
                " [" + CommentsColumns.SOURCE_ACCESS_KEY + "] TEXT, " +
                " CONSTRAINT [] UNIQUE ([" + CommentsColumns.COMMENT_ID + "]," +
                " [" + CommentsColumns.FROM_ID + "], [" + CommentsColumns.SOURCE_ID + "]," +
                " [" + CommentsColumns.SOURCE_OWNER_ID + "], [" + CommentsColumns.SOURCE_TYPE + "]) ON CONFLICT REPLACE);";

        db.execSQL(create);
    }

    /**
     * Создание таблицы закладок фото
     *
     * @param db БД
     */
    private void createFavePhotosTable(SQLiteDatabase db) {
        String create = "CREATE TABLE [" + FavePhotosColumns.TABLENAME + "] (" +
                " [" + FavePhotosColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + FavePhotosColumns.PHOTO_ID + "] INTEGER, " +
                " [" + FavePhotosColumns.OWNER_ID + "] INTEGER, " +
                " [" + FavePhotosColumns.POST_ID + "] INTEGER, " +
                " [" + FavePhotosColumns.PHOTO + "] BLOB, " +
                " CONSTRAINT [] UNIQUE ([" + FavePhotosColumns.PHOTO_ID + "], [" + FavePhotosColumns.OWNER_ID + "]) ON CONFLICT REPLACE);";

        db.execSQL(create);
    }

    /**
     * Создание таблицы закладок видео
     *
     * @param db БД
     */
    private void createFaveVideosTable(SQLiteDatabase db) {
        String create = "CREATE TABLE [" + FaveVideosColumns.TABLENAME + "] (" +
                " [" + FaveVideosColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + FaveVideosColumns.VIDEO + "] BLOB);";
        db.execSQL(create);
    }

    /**
     * Создание таблицы закладок постов
     *
     * @param db БД
     */
    private void createFavePostsTable(SQLiteDatabase db) {
        String create = "CREATE TABLE [" + FavePostsColumns.TABLENAME + "] (" +
                " [" + FavePostsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + FavePostsColumns.POST + "] BLOB);";
        db.execSQL(create);
    }

    /**
     * Создание таблицы закладок пользователей
     *
     * @param db БД
     */
    private void createFaveUsersTable(SQLiteDatabase db) {
        String create = "CREATE TABLE [" + FaveUsersColumns.TABLENAME + "] (" +
                " [" + FaveUsersColumns._ID + "] BIGINT NOT NULL UNIQUE, " +
                " CONSTRAINT [] PRIMARY KEY([" + FaveUsersColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(create);
    }

    /**
     * Создание таблицы закладок ссылок
     *
     * @param db БД
     */
    private void createFaveLinksTable(SQLiteDatabase db) {
        String create = "CREATE TABLE [" + FaveLinksColumns.TABLENAME + "] (" +
                " [" + FaveLinksColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + FaveLinksColumns.LINK_ID + "] TEXT, " +
                " [" + FaveLinksColumns.URL + "] TEXT, " +
                " [" + FaveLinksColumns.TITLE + "] TEXT, " +
                " [" + FaveLinksColumns.DESCRIPTION + "] TEXT, " +
                " [" + FaveLinksColumns.PHOTO_50 + "] TEXT, " +
                " [" + FaveLinksColumns.PHOTO_100 + "] TEXT, " +
                " CONSTRAINT [] UNIQUE ([" + FaveLinksColumns.LINK_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(create);
    }

    /**
     * Создание таблицы вложений для комментариев
     *
     * @param db БД
     */
    private void createCommentsAttachmentsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + CommentsAttachmentsColumns.TABLENAME + "] (\n" +
                " [" + CommentsAttachmentsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + CommentsAttachmentsColumns.C_ID + "] INTEGER, " +
                " [" + CommentsAttachmentsColumns.TYPE + "] INTEGER, " +
                " [" + CommentsAttachmentsColumns.DATA + "] TEXT, " +
                //" [" + CommentsAttachmentsColumns.ATTACHMENT_ID + "] INTEGER, " +
                //" [" + CommentsAttachmentsColumns.ATTACHMENT_OWNER_ID + "] INTEGER, " +
                //" CONSTRAINT [] UNIQUE ([" + CommentsAttachmentsColumns.C_ID + "], [" + CommentsAttachmentsColumns.ATTACHMENT_ID + "], [" + CommentsAttachmentsColumns.ATTACHMENT_OWNER_ID + "], [" + CommentsAttachmentsColumns.TYPE + "]) ON CONFLICT REPLACE," +
                " FOREIGN KEY([" + CommentsAttachmentsColumns.C_ID + "]) " +
                " REFERENCES " + CommentsColumns.TABLENAME + "([" + CommentsColumns._ID + "]) ON DELETE CASCADE ON UPDATE CASCADE);";
        db.execSQL(sql);
    }

    /**
     * Создание таблицы вложений для постов
     *
     * @param db БД
     */
    private void createPostAttachmentsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + PostAttachmentsColumns.TABLENAME + "] (\n" +
                " [" + PostsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + PostAttachmentsColumns.P_ID + "] INTEGER, " +
                " [" + PostAttachmentsColumns.TYPE + "] INTEGER, " +
                " [" + PostAttachmentsColumns.DATA + "] TEXT, " +
                " FOREIGN KEY([" + PostAttachmentsColumns.P_ID + "]) " +
                " REFERENCES " + PostsColumns.TABLENAME + "([" + PostsColumns._ID + "]) ON DELETE CASCADE ON UPDATE CASCADE);";
        db.execSQL(sql);
    }

    private void createMessagesTable(SQLiteDatabase db) {
        String create = "CREATE TABLE [" + MessageColumns.TABLENAME + "] (\n" +
                " [" + MessageColumns._ID + "] INTEGER PRIMARY KEY ON CONFLICT REPLACE AUTOINCREMENT NOT NULL UNIQUE, " +
                " [" + MessageColumns.PEER_ID + "] INTEGER, " +
                " [" + MessageColumns.FROM_ID + "] INTEGER, " +
                " [" + MessageColumns.DATE + "] BIGINT, " +
                " [" + MessageColumns.READ_STATE + "] BOOLEAN, " +
                " [" + MessageColumns.OUT + "] BOOLEAN, " +
                " [" + MessageColumns.TITLE + "] TEXT, " +
                " [" + MessageColumns.BODY + "] TEXT, " +
                " [" + MessageColumns.ENCRYPTED + "] BOOLEAN, " +
                " [" + MessageColumns.DELETED + "] BOOLEAN, " +
                " [" + MessageColumns.IMPORTANT + "] BOOLEAN, " +
                " [" + MessageColumns.FORWARD_COUNT + "] INTEGER, " +
                " [" + MessageColumns.HAS_ATTACHMENTS + "] BOOLEAN, " +
                " [" + MessageColumns.ATTACH_TO + "] INTEGER REFERENCES " + MessageColumns.TABLENAME + "([" + MessageColumns._ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, " +
                " [" + MessageColumns.STATUS + "] INTEGER, " +
                " [" + MessageColumns.CHAT_ACTIVE + "] TEXT, " +
                //" [" + MessageColumns.PUSH_SETTINGS + "] TEXT, " +
                " [" + MessageColumns.USER_COUNT + "] INTEGER, " +
                " [" + MessageColumns.ADMIN_ID + "] INTEGER, " +
                " [" + MessageColumns.ACTION + "] INTEGER, " +
                " [" + MessageColumns.ACTION_MID + "] INTEGER, " +
                " [" + MessageColumns.ACTION_EMAIL + "] TEXT, " +
                " [" + MessageColumns.ACTION_TEXT + "] TEXT, " +
                " [" + MessageColumns.PHOTO_50 + "] TEXT, " +
                " [" + MessageColumns.PHOTO_100 + "] TEXT, " +
                " [" + MessageColumns.PHOTO_200 + "] TEXT, " +
                " [" + MessageColumns.RANDOM_ID + "] INTEGER, " +
                " [" + MessageColumns.EXTRAS + "] TEXT, " +
                " [" + MessageColumns.ORIGINAL_ID + "] INTEGER);";

        String insertZeroRow = "INSERT INTO " + MessageColumns.TABLENAME + " (" + MessageColumns._ID + ") VALUES (0)";
        String insert = "INSERT INTO " + MessageColumns.TABLENAME + " (" + MessageColumns._ID + ") VALUES (1000000000)";
        String delete = "DELETE FROM " + MessageColumns.TABLENAME + " WHERE " + MessageColumns._ID + " = 1000000000";

        db.execSQL(create);
        db.execSQL(insertZeroRow);
        db.execSQL(insert);
        db.execSQL(delete);
    }

    private void createFriendListsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + FriendListsColumns.TABLENAME + "] (\n" +
                "  [" + FriendListsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + FriendListsColumns.USER_ID + "] INTEGER, " +
                "  [" + FriendListsColumns.LIST_ID + "] INTEGER, " +
                "  [" + FriendListsColumns.NAME + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + FriendListsColumns.USER_ID + "], [" + FriendListsColumns.LIST_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createVideosTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + VideoColumns.TABLENAME + "] (\n" +
                "  [" + VideoColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + VideoColumns.VIDEO_ID + "] INTEGER, " +
                "  [" + VideoColumns.OWNER_ID + "] INTEGER, " +
                "  [" + VideoColumns.ORIGINAL_OWNER_ID + "] INTEGER, " +
                "  [" + VideoColumns.ALBUM_ID + "] INTEGER, " +
                "  [" + VideoColumns.TITLE + "] TEXT, " +
                "  [" + VideoColumns.DESCRIPTION + "] TEXT, " +
                "  [" + VideoColumns.DURATION + "] INTEGER, " +
                "  [" + VideoColumns.LINK + "] TEXT, " +
                "  [" + VideoColumns.DATE + "] INTEGER, " +
                "  [" + VideoColumns.ADDING_DATE + "] INTEGER, " +
                "  [" + VideoColumns.VIEWS + "] INTEGER, " +
                "  [" + VideoColumns.PLAYER + "] TEXT, " +
                "  [" + VideoColumns.PHOTO_130 + "] TEXT, " +
                "  [" + VideoColumns.PHOTO_320 + "] TEXT, " +
                "  [" + VideoColumns.PHOTO_800 + "] TEXT, " +
                "  [" + VideoColumns.ACCESS_KEY + "] TEXT, " +
                "  [" + VideoColumns.COMMENTS + "] INTEGER, " +
                "  [" + VideoColumns.CAN_COMENT + "] INTEGER, " +
                "  [" + VideoColumns.CAN_REPOST + "] INTEGER, " +
                "  [" + VideoColumns.USER_LIKES + "] INTEGER, " +
                "  [" + VideoColumns.REPEAT + "] INTEGER, " +
                "  [" + VideoColumns.LIKES + "] INTEGER, " +
                "  [" + VideoColumns.PRIVACY_VIEW + "] TEXT, " +
                "  [" + VideoColumns.PRIVACY_COMMENT + "] TEXT, " +
                "  [" + VideoColumns.MP4_240 + "] TEXT, " +
                "  [" + VideoColumns.MP4_360 + "] TEXT, " +
                "  [" + VideoColumns.MP4_480 + "] TEXT, " +
                "  [" + VideoColumns.MP4_720 + "] TEXT, " +
                "  [" + VideoColumns.MP4_1080 + "] TEXT, " +
                "  [" + VideoColumns.EXTERNAL + "] TEXT, " +
                "  [" + VideoColumns.PLATFORM + "] TEXT, " +
                "  [" + VideoColumns.CAN_EDIT + "] BOOLEAN, " +
                "  [" + VideoColumns.CAN_ADD + "] BOOLEAN, " +
                "  CONSTRAINT [] UNIQUE ([" + VideoColumns.VIDEO_ID + "], [" + VideoColumns.OWNER_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createDocsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + DocColumns.TABLENAME + "] (\n" +
                "  [" + DocColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + DocColumns.DOC_ID + "] INTEGER, " +
                "  [" + DocColumns.OWNER_ID + "] BIGINT, " +
                "  [" + DocColumns.TITLE + "] TEXT, " +
                "  [" + DocColumns.SIZE + "] INTEGER, " +
                "  [" + DocColumns.EXT + "] TEXT, " +
                "  [" + DocColumns.URL + "] TEXT, " +
                "  [" + DocColumns.DATE + "] BIGINT, " +
                "  [" + DocColumns.TYPE + "] INTEGER, " +
                "  [" + DocColumns.PHOTO + "] TEXT, " +
                "  [" + DocColumns.GRAFFITI + "] TEXT, " +
                "  [" + DocColumns.VIDEO + "] TEXT, " +
                "  [" + DocColumns.AUDIO + "] TEXT, " +
                "  [" + DocColumns.ACCESS_KEY + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + DocColumns.DOC_ID + "], [" + DocColumns.OWNER_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createDialogTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + DialogsColumns.TABLENAME + "] (\n" +
                "  [" + DialogsColumns._ID + "] INTEGER PRIMARY KEY ON CONFLICT REPLACE AUTOINCREMENT NOT NULL UNIQUE, " +
                "  [" + DialogsColumns.UNREAD + "] INTEGER, " +
                "  [" + DialogsColumns.TITLE + "] TEXT, " +
                "  [" + DialogsColumns.PHOTO_50 + "] TEXT, " +
                "  [" + DialogsColumns.PHOTO_100 + "] TEXT, " +
                "  [" + DialogsColumns.PHOTO_200 + "] TEXT, " +
                "  [" + DialogsColumns.LAST_MESSAGE_ID + "] INTEGER, " +
                "  [" + DialogsColumns.ADMIN_ID + "] INTEGER);";
        db.execSQL(sql);
    }

    private void createRelativeshipTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + RelationshipColumns.TABLENAME + "] (" +
                "  [" + RelationshipColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + RelationshipColumns.OBJECT_ID + "] BIGINT NOT NULL, " +
                "  [" + RelationshipColumns.SUBJECT_ID + "] BIGINT NOT NULL, " +
                "  [" + RelationshipColumns.TYPE + "] INTEGER, " +
                "  CONSTRAINT [] UNIQUE ([" + RelationshipColumns.OBJECT_ID + "], [" + RelationshipColumns.SUBJECT_ID + "], [" + RelationshipColumns.TYPE + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createUsersTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + UserColumns.TABLENAME + "](" +
                " [" + UserColumns._ID + "] INTEGER NOT NULL UNIQUE, " +
                " [" + UserColumns.FIRST_NAME + "] TEXT, " +
                " [" + UserColumns.LAST_NAME + "] TEXT, " +
                " [" + UserColumns.ONLINE + "] BOOLEAN, " +
                " [" + UserColumns.ONLINE_MOBILE + "] BOOLEAN, " +
                " [" + UserColumns.ONLINE_APP + "] INTEGER, " +
                " [" + UserColumns.PHOTO_50 + "] TEXT, " +
                " [" + UserColumns.PHOTO_100 + "] TEXT, " +
                " [" + UserColumns.PHOTO_200 + "] TEXT, " +
                " [" + UserColumns.LAST_SEEN + "] BIGINT, " +
                " [" + UserColumns.PLATFORM + "] INTEGER, " +
                " [" + UserColumns.USER_STATUS + "] TEXT, " +
                " [" + UserColumns.SEX + "] INTEGER, " +
                " [" + UserColumns.DOMAIN + "] TEXT, " +
                " [" + UserColumns.IS_FRIEND + "] BOOLEAN, " +
                " [" + UserColumns.FRIEND_STATUS + "] INTEGER, " +
                " CONSTRAINT [] PRIMARY KEY([" + UserColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    /* Триггер, который удаляет историю репостов при удалении самого поста */
    /*private void createDeleteWallCopyHistoryTrigger(SQLiteDatabase db) {
        String sql = "CREATE TRIGGER [t_delete_wall_copy_history] AFTER DELETE ON [" + PostsColumns.TABLENAME + "] " +
                " WHEN [old].[" + PostsColumns.HAS_COPY_HISTORY + "] = 1 " +
                " BEGIN " +
                " DELETE FROM [" + PostsColumns.TABLENAME + "] " +
                " WHERE [" + PostsColumns.COPY_HISTORY_OF + "] = [old].[" + PostsColumns._ID + "] " +
                " AND [" + PostsColumns.COPY_PARENT_TYPE + "] = " + PostsColumns.COPY_PARENT_TYPE_WALL + ";" +
                " END;";
        db.execSQL(sql);
    }*/

    /*private void createDeleteFeedCopyHistoryTrigger(SQLiteDatabase db) {
        String sql = "CREATE TRIGGER [t_delete_feed_copy_history] AFTER DELETE ON [" + NewsColumns.TABLENAME + "] " +
                " WHEN [old].[" + NewsColumns.HAS_COPY_HISTORY + "] = 1 " +
                " BEGIN " +
                " DELETE FROM [" + PostsColumns.TABLENAME + "] " +
                " WHERE [" + PostsColumns.COPY_HISTORY_OF + "] = [old].[" + NewsColumns._ID + "] " +
                " AND [" + PostsColumns.COPY_PARENT_TYPE + "] = " + PostsColumns.COPY_PARENT_TYPE_FEED + ";" +
                " END;";
        db.execSQL(sql);
    }*/

    private void createNewsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + NewsColumns.TABLENAME + "] (\n" +
                "  [" + NewsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + NewsColumns.TYPE + "] TEXT, " +
                "  [" + NewsColumns.SOURCE_ID + "] INTEGER, " +
                "  [" + NewsColumns.DATE + "] INTEGER, " +
                "  [" + NewsColumns.POST_ID + "] INTEGER, " +
                "  [" + NewsColumns.POST_TYPE + "] TEXT, " +
                "  [" + NewsColumns.FINAL_POST + "] BOOLEAN, " +
                "  [" + NewsColumns.COPY_OWNER_ID + "] INTEGER, " +
                "  [" + NewsColumns.COPY_POST_ID + "] INTEGER, " +
                "  [" + NewsColumns.COPY_POST_DATE + "] INTEGER, " +
                "  [" + NewsColumns.TEXT + "] TEXT, " +
                "  [" + NewsColumns.CAN_EDIT + "] BOOLEAN, " +
                "  [" + NewsColumns.CAN_DELETE + "] BOOLEAN, " +
                "  [" + NewsColumns.COMMENT_COUNT + "] INTEGER, " +
                "  [" + NewsColumns.COMMENT_CAN_POST + "] BOOLEAN, " +
                "  [" + NewsColumns.LIKE_COUNT + "] INTEGER, " +
                "  [" + NewsColumns.USER_LIKE + "] BOOLEAN, " +
                "  [" + NewsColumns.CAN_LIKE + "] BOOLEAN, " +
                "  [" + NewsColumns.CAN_PUBLISH + "] BOOLEAN, " +
                "  [" + NewsColumns.REPOSTS_COUNT + "] INTEGER, " +
                "  [" + NewsColumns.USER_REPOSTED + "] BOOLEAN, " +
                //"  [" + NewsColumns.ATTACHMENTS_MASK + "] INTEGER, " +
                "  [" + NewsColumns.GEO_ID + "] INTEGER, " +
                "  [" + NewsColumns.ATTACHMENTS_JSON + "] TEXT, " +
                "  [" + NewsColumns.VIEWS + "] INTEGER, " +
                "  [" + NewsColumns.TAG_FRIENDS + "] TEXT);";
        db.execSQL(sql);
    }

    private void createUserCareerTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + UserCareerColumns.TABLENAME + "] (\n" +
                "  [" + UserCareerColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + UserCareerColumns.USER_ID + "] INTEGER, " +
                "  [" + UserCareerColumns.GROUP_ID + "] INTEGER, " +
                "  [" + UserCareerColumns.COMPANY + "] VARCHAR(2048), " +
                "  [" + UserCareerColumns.COUNTRY_ID + "] INTEGER, " +
                "  [" + UserCareerColumns.CITY_ID + "] INTEGER, " +
                "  [" + UserCareerColumns.CITY_NAME + "] VARCHAR(2048), " +
                "  [" + UserCareerColumns.YEAR_FROM + "] INTEGER, " +
                "  [" + UserCareerColumns.YEAR_UNTIL + "] INTEGER, " +
                "  [" + UserCareerColumns.POSITION + "] VARCHAR(2048));";
        db.execSQL(sql);
    }

    private void createPostsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + PostsColumns.TABLENAME + "] (\n" +
                "  [" + PostsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + PostsColumns.POST_ID + "] INTEGER, " +
                "  [" + PostsColumns.OWNER_ID + "] INTEGER, " +
                "  [" + PostsColumns.FROM_ID + "] INTEGER, " +
                "  [" + PostsColumns.DATE + "] INTEGER, " +
                "  [" + PostsColumns.TEXT + "] TEXT, " +
                "  [" + PostsColumns.REPLY_OWNER_ID + "] INTEGER, " +
                "  [" + PostsColumns.REPLY_POST_ID + "] INTEGER, " +
                "  [" + PostsColumns.FRIENDS_ONLY + "] BOOLEAN, " +
                "  [" + PostsColumns.COMMENTS_COUNT + "] INTEGER, " +
                "  [" + PostsColumns.CAN_POST_COMMENT + "] BOOLEAN, " +
                "  [" + PostsColumns.LIKES_COUNT + "] INTEGER, " +
                "  [" + PostsColumns.USER_LIKES + "] BOOLEAN, " +
                "  [" + PostsColumns.CAN_LIKE + "] BOOLEAN, " +
                "  [" + PostsColumns.CAN_PUBLISH + "] BOOLEAN, " +
                "  [" + PostsColumns.CAN_EDIT + "] BOOLEAN, " +
                "  [" + PostsColumns.REPOSTS_COUNT + "] INTEGER, " +
                "  [" + PostsColumns.USER_REPOSTED + "] BOOLEAN, " +
                "  [" + PostsColumns.POST_TYPE + "] TEXT, " +
                "  [" + PostsColumns.ATTACHMENTS_MASK + "] INTEGER, " +
                "  [" + PostsColumns.SIGNED_ID + "] INTEGER, " +
                "  [" + PostsColumns.CREATED_BY + "] INTEGER, " +
                "  [" + PostsColumns.CAN_PIN + "] BOOLEAN, " +
                "  [" + PostsColumns.IS_PINNED + "] BOOLEAN, " +
                "  [" + PostsColumns.DELETED + "] BOOLEAN, " +
                "  [" + PostsColumns.POST_SOURCE + "] TEXT, " +
                "  [" + PostsColumns.VIEWS + "] INTEGER, " +
                "  CONSTRAINT [] UNIQUE ([" + PostsColumns.POST_ID + "], [" + PostsColumns.OWNER_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createGroupsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + GroupColumns.TABLENAME + "](" +
                " [" + GroupColumns._ID + "] INTEGER NOT NULL UNIQUE, " +
                " [" + GroupColumns.NAME + "] TEXT, " +
                " [" + GroupColumns.SCREEN_NAME + "] TEXT, " +
                " [" + GroupColumns.IS_CLOSED + "] INTEGER, " +
                " [" + GroupColumns.IS_ADMIN + "] BOOLEAN, " +
                " [" + GroupColumns.ADMIN_LEVEL + "] INTEGER, " +
                " [" + GroupColumns.IS_MEMBER + "] BOOLEAN, " +
                " [" + GroupColumns.MEMBER_STATUS + "] INTEGER, " +
                " [" + GroupColumns.TYPE + "] INTEGER, " +
                " [" + GroupColumns.PHOTO_50 + "] TEXT, " +
                " [" + GroupColumns.PHOTO_100 + "] TEXT, " +
                " [" + GroupColumns.PHOTO_200 + "] TEXT, " +
                " [" + GroupColumns.CAN_ADD_TOPICS + "] BOOLEAN, " +
                " [" + GroupColumns.TOPICS_ORDER + "] BOOLEAN, " +
                " CONSTRAINT [] PRIMARY KEY([" + GroupColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createGroupsDetTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + GroupsDetColumns.TABLENAME + "] (\n" +
                " [" + GroupsDetColumns._ID + "] INTEGER NOT NULL UNIQUE, " +
                " [" + GroupsDetColumns.BLACKLISTED + "] BOOLEAN, " +
                " [" + GroupsDetColumns.BAN_END_DATE + "] BIGINT, " +
                " [" + GroupsDetColumns.BAN_COMEMNT + "] TEXT, " +
                " [" + GroupsDetColumns.CITY_ID + "] INTEGER, " +
                " [" + GroupsDetColumns.COUNTRY_ID + "] INTEGER, " +
                " [" + GroupsDetColumns.GEO_ID + "] INTEGER, " +
                " [" + GroupsDetColumns.DESCRIPTION + "] TEXT, " +
                " [" + GroupsDetColumns.WIKI_PAGE + "] TEXT, " +
                " [" + GroupsDetColumns.MEMBERS_COUNT + "] INTEGER, " +
                " [" + GroupsDetColumns.COUNTERS + "] BLOB, " +
                " [" + GroupsDetColumns.START_DATE + "] BIGINT, " +
                " [" + GroupsDetColumns.FINISH_DATE + "] BIGINT, " +
                " [" + GroupsDetColumns.CAN_POST + "] BOOLEAN, " +
                " [" + GroupsDetColumns.CAN_SEE_ALL_POSTS + "] BOOLEAN, " +
                " [" + GroupsDetColumns.CAN_UPLOAD_DOC + "] BOOLEAN, " +
                " [" + GroupsDetColumns.CAN_UPLOAD_VIDEO + "] BOOLEAN, " +
                " [" + GroupsDetColumns.CAN_CREATE_TOPIC + "] BOOLEAN, " +
                " [" + GroupsDetColumns.ACTIVITY + "] TEXT, " +
                " [" + GroupsDetColumns.STATUS + "] TEXT, " +
                " [" + GroupsDetColumns.FIXED_POST + "] INTEGER, " +
                " [" + GroupsDetColumns.VERIFIED + "] BOOLEAN, " +
                " [" + GroupsDetColumns.SITE + "] TEXT, " +
                " [" + GroupsDetColumns.MAIN_ALBUM_ID + "] INTEGER, " +
                " [" + GroupsDetColumns.IS_FAVORITE + "] BOOLEAN, " +
                " [" + GroupsDetColumns.LINKS_COUNT + "] INTEGER, " +
                " [" + GroupsDetColumns.CONTACTS_COUNT + "] INTEGER, " +
                " [" + GroupsDetColumns.CAN_MESSAGE + "] BOOLEAN, " +
                " CONSTRAINT [] PRIMARY KEY([" + GroupsDetColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createUserDetTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + UsersDetColumns.TABLENAME + "] (\n" +
                " [" + UsersDetColumns._ID + "] INTEGER NOT NULL UNIQUE, " +
                " [" + UsersDetColumns.DATA + "] TEXT, " +
                " CONSTRAINT [] PRIMARY KEY([" + UsersDetColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createPhotoAlbumsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + PhotoAlbumsColumns.TABLENAME + "] (\n" +
                "  [" + PhotoAlbumsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + PhotoAlbumsColumns.ALBUM_ID + "] INTEGER, " +
                "  [" + PhotoAlbumsColumns.OWNER_ID + "] INTEGER, " +
                "  [" + PhotoAlbumsColumns.TITLE + "] TEXT, " +
                "  [" + PhotoAlbumsColumns.SIZE + "] INTEGER, " +
                "  [" + PhotoAlbumsColumns.PRIVACY_VIEW + "] TEXT, " +
                "  [" + PhotoAlbumsColumns.PRIVACY_COMMENT + "] TEXT, " +
                "  [" + PhotoAlbumsColumns.DESCRIPTION + "] TEXT, " +
                "  [" + PhotoAlbumsColumns.CAN_UPLOAD + "] BOOLEAN, " +
                "  [" + PhotoAlbumsColumns.UPDATED + "] BIGINT, " +
                "  [" + PhotoAlbumsColumns.CREATED + "] BIGINT, " +
                "  [" + PhotoAlbumsColumns.SIZES + "] TEXT, " +
                "  [" + PhotoAlbumsColumns.UPLOAD_BY_ADMINS + "] BOOLEAN, " +
                "  [" + PhotoAlbumsColumns.COMMENTS_DISABLED + "] BOOLEAN, " +
                "  CONSTRAINT [] UNIQUE ([" + PhotoAlbumsColumns.ALBUM_ID + "], [" + PhotoAlbumsColumns.OWNER_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createVideoAlbumsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + VideoAlbumsColumns.TABLENAME + "] (\n" +
                "  [" + VideoAlbumsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + VideoAlbumsColumns.ALBUM_ID + "] INTEGER, " +
                "  [" + VideoAlbumsColumns.OWNER_ID + "] INTEGER, " +
                "  [" + VideoAlbumsColumns.TITLE + "] VARCHAR(2048), " +
                "  [" + VideoAlbumsColumns.COUNT + "] INTEGER, " +
                "  [" + VideoAlbumsColumns.PHOTO_160 + "] VARCHAR(2048), " +
                "  [" + VideoAlbumsColumns.PHOTO_320 + "] VARCHAR(2048), " +
                "  [" + VideoAlbumsColumns.UPDATE_TIME + "] BIGINT, " +
                "  [" + VideoAlbumsColumns.PRIVACY + "] VARCHAR(2048), " +
                "  CONSTRAINT [] UNIQUE ([" + VideoAlbumsColumns.ALBUM_ID + "], [" + VideoAlbumsColumns.OWNER_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createTopicsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + TopicsColumns.TABLENAME + "] (\n" +
                "  [" + TopicsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + TopicsColumns.TOPIC_ID + "] INTEGER, " +
                "  [" + TopicsColumns.OWNER_ID + "] INTEGER, " +
                "  [" + TopicsColumns.TITLE + "] VARCHAR(2048), " +
                "  [" + TopicsColumns.CREATED + "] BIGINT, " +
                "  [" + TopicsColumns.CREATED_BY + "] INTEGER, " +
                "  [" + TopicsColumns.UPDATED + "] BIGINT, " +
                "  [" + TopicsColumns.UPDATED_BY + "] INTEGER, " +
                "  [" + TopicsColumns.IS_CLOSED + "] BOOLEAN, " +
                "  [" + TopicsColumns.IS_FIXED + "] BOOLEAN, " +
                "  [" + TopicsColumns.COMMENTS + "] INTEGER, " +
                "  [" + TopicsColumns.FIRST_COMMENT + "] VARCHAR(2048), " +
                "  [" + TopicsColumns.LAST_COMMENT + "] VARCHAR(2048), " +
                "  [" + TopicsColumns.ATTACHED_POLL + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + TopicsColumns.TOPIC_ID + "], [" + TopicsColumns.OWNER_ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    private void createNotoficationsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + NotificationColumns.TABLENAME + "] (\n" +
                "  [" + AttachmentsColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + NotificationColumns.TYPE + "] TEXT, " +
                "  [" + NotificationColumns.DATE + "] INTEGER, " +
                "  [" + NotificationColumns.DATA + "] TEXT);";
        db.execSQL(sql);
    }

    private void createFeedListsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE [" + FeedListsColumns.TABLENAME + "] (\n" +
                " [" + FeedListsColumns._ID + "] INTEGER NOT NULL UNIQUE, " +
                " [" + FeedListsColumns.TITLE + "] TEXT, " +
                " [" + FeedListsColumns.NO_REPOSTS + "] BOOLEAN, " +
                " [" + FeedListsColumns.SOURCE_IDS + "] TEXT, " +
                " CONSTRAINT [] PRIMARY KEY([" + UserColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }
}
