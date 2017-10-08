package biz.dealnote.messenger.db.impl;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.FriendListsColumns;
import biz.dealnote.messenger.db.column.GroupColumns;
import biz.dealnote.messenger.db.column.UserColumns;
import biz.dealnote.messenger.db.column.UsersDetColumns;
import biz.dealnote.messenger.db.interfaces.IOwnersStore;
import biz.dealnote.messenger.db.model.BanAction;
import biz.dealnote.messenger.db.model.UserPatch;
import biz.dealnote.messenger.db.model.entity.CommunityEntity;
import biz.dealnote.messenger.db.model.entity.FriendListEntity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.UserDetailsEntity;
import biz.dealnote.messenger.db.model.entity.UserEntity;
import biz.dealnote.messenger.fragment.UserInfoResolveUtil;
import biz.dealnote.messenger.model.Manager;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

import static android.text.TextUtils.join;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

class OwnersRepositiry extends AbsStore implements IOwnersStore {

    private final PublishSubject<BanAction> banActionsPublisher;
    private final PublishSubject<Pair<Integer, Manager>> managementActionsPublisher;

    OwnersRepositiry(@NonNull AppStores context) {
        super(context);
        this.banActionsPublisher = PublishSubject.create();
        this.managementActionsPublisher = PublishSubject.create();
    }

    private static void appendUserInsertOperation(@NonNull List<ContentProviderOperation> operations, @NonNull Uri uri, UserEntity dbo) {
        operations.add(ContentProviderOperation.newInsert(uri)
                .withValues(createCv(dbo))
                .build());
    }

    private static void appendCommunityInsertOperation(@NonNull List<ContentProviderOperation> operations, @NonNull Uri uri, CommunityEntity dbo) {
        operations.add(ContentProviderOperation.newInsert(uri)
                .withValues(createCv(dbo))
                .build());
    }

    static void appendOwnersInsertOperations(@NonNull List<ContentProviderOperation> operations, int accountId, OwnerEntities ownerEntities){
        appendUsersInsertOperation(operations, accountId, ownerEntities.getUserEntities());
        appendCommunitiesInsertOperation(operations, accountId, ownerEntities.getCommunityEntities());
    }

    static void appendUsersInsertOperation(@NonNull List<ContentProviderOperation> operations, int accouuntId, List<UserEntity> dbos){
        final Uri uri = MessengerContentProvider.getUserContentUriFor(accouuntId);
        for(UserEntity dbo : dbos){
            appendUserInsertOperation(operations, uri, dbo);
        }
    }

    static void appendCommunitiesInsertOperation(@NonNull List<ContentProviderOperation> operations, int accouuntId, List<CommunityEntity> dbos){
        final Uri uri = MessengerContentProvider.getGroupsContentUriFor(accouuntId);
        for(CommunityEntity dbo : dbos){
            appendCommunityInsertOperation(operations, uri, dbo);
        }
    }

    private static ContentValues createCv(CommunityEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(GroupColumns._ID, dbo.getId());
        cv.put(GroupColumns.NAME, dbo.getName());
        cv.put(GroupColumns.SCREEN_NAME, dbo.getScreenName());
        cv.put(GroupColumns.IS_CLOSED, dbo.getClosed());
        cv.put(GroupColumns.IS_ADMIN, dbo.isAdmin());
        cv.put(GroupColumns.ADMIN_LEVEL, dbo.getAdminLevel());
        cv.put(GroupColumns.IS_MEMBER, dbo.isMember());
        cv.put(GroupColumns.MEMBER_STATUS, dbo.getMemberStatus());
        cv.put(GroupColumns.TYPE, dbo.getType());
        cv.put(GroupColumns.PHOTO_50, dbo.getPhoto50());
        cv.put(GroupColumns.PHOTO_100, dbo.getPhoto100());
        cv.put(GroupColumns.PHOTO_200, dbo.getPhoto200());
        return cv;
    }

    private static ContentValues createCv(UserEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(UserColumns._ID, dbo.getId());
        cv.put(UserColumns.FIRST_NAME, dbo.getFirstName());
        cv.put(UserColumns.LAST_NAME, dbo.getLastName());
        cv.put(UserColumns.ONLINE, dbo.isOnline());
        cv.put(UserColumns.ONLINE_MOBILE, dbo.isOnlineMobile());
        cv.put(UserColumns.ONLINE_APP, dbo.getOnlineApp());
        cv.put(UserColumns.PHOTO_50, dbo.getPhoto50());
        cv.put(UserColumns.PHOTO_100, dbo.getPhoto100());
        cv.put(UserColumns.PHOTO_200, dbo.getPhoto200());
        cv.put(UserColumns.LAST_SEEN, dbo.getLastSeen());
        cv.put(UserColumns.PLATFORM, dbo.getPlatform());
        cv.put(UserColumns.USER_STATUS, dbo.getStatus());
        cv.put(UserColumns.SEX, dbo.getSex());
        cv.put(UserColumns.DOMAIN, dbo.getDomain());
        cv.put(UserColumns.IS_FRIEND, dbo.isFriend());
        return cv;
    }

    @Override
    public Completable fireBanAction(BanAction action) {
        return Completable.fromAction(() -> banActionsPublisher.onNext(action));
    }

    @Override
    public Observable<BanAction> observeBanActions() {
        return banActionsPublisher;
    }

    @Override
    public Completable fireManagementChangeAction(Pair<Integer, Manager> manager) {
        return Completable.fromAction(() -> managementActionsPublisher.onNext(manager));
    }

    @Override
    public Observable<Pair<Integer, Manager>> observeManagementChanges() {
        return managementActionsPublisher;
    }

    @Override
    public Single<Optional<UserDetailsEntity>> getUserDetails(int accountId, int userId) {
        return Single.fromCallable(() -> {
            final Uri uri = MessengerContentProvider.getUserDetContentUriFor(accountId);
            final String where = UsersDetColumns._ID + " = ?";
            final String[] args = {String.valueOf(userId)};

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);
            UserDetailsEntity details = null;

            if(nonNull(cursor)){
                if(cursor.moveToNext()){
                    String json = cursor.getString(cursor.getColumnIndex(UsersDetColumns.DATA));
                    if(nonEmpty(json)){
                        details = GSON.fromJson(json, UserDetailsEntity.class);
                    }
                }

                cursor.close();
            }

            return Optional.wrap(details);
        });
    }

    @Override
    public Completable storeUserDetails(int accountId, int userId, UserDetailsEntity dbo) {
        return Completable.fromAction(() -> {
            ContentValues cv = new ContentValues();
            cv.put(UsersDetColumns._ID, userId);
            cv.put(UsersDetColumns.DATA, GSON.toJson(dbo));

            final Uri uri = MessengerContentProvider.getUserDetContentUriFor(accountId);

            getContentResolver().insert(uri, cv);
        });
    }

    @Override
    public Completable updateUser(int accountId, int userId, UserPatch patch) {
        return Completable.fromAction(() -> {
            ContentValues maincv = new ContentValues();

            if(nonNull(patch.getStatusUpdate())){
                maincv.put(UserColumns.USER_STATUS, patch.getStatusUpdate().getStatus());
            }

            if(maincv.size() > 0){
                Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);
                getContentResolver().update(uri, maincv, UserColumns._ID + " = ?", new String[]{String.valueOf(userId)});
            }
        });
    }

    @Override
    public Single<Map<Integer, FriendListEntity>> findFriendsListsByIds(int accountId, int userId, Collection<Integer> ids) {
        return Single.create(emitter -> {
            final Uri uri = MessengerContentProvider.getFriendListsContentUriFor(accountId);

            String where = FriendListsColumns.USER_ID + " = ? " + " AND " + FriendListsColumns.LIST_ID + " IN(" + join(",", ids) + ")";
            String[] args = {String.valueOf(userId)};

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            @SuppressLint("UseSparseArrays")
            Map<Integer, FriendListEntity> map = new HashMap<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if(emitter.isDisposed()){
                        break;
                    }

                    FriendListEntity dbo = mapFriendsList(cursor);
                    map.put(dbo.getId(), dbo);
                }

                cursor.close();
            }

            emitter.onSuccess(map);
        });
    }

    @Override
    public Maybe<String> getLocalizedUserActivity(int accountId, int userId) {
        return Maybe.create(e -> {
            String[] uProjection = {UserColumns.LAST_SEEN, UserColumns.ONLINE, UserColumns.SEX};
            Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);
            String where = UserColumns._ID + " = ?";
            String[] args = {String.valueOf(userId)};
            Cursor cursor = getContext().getContentResolver().query(uri, uProjection, where, args, null);

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    boolean online = cursor.getInt(cursor.getColumnIndex(UserColumns.ONLINE)) == 1;
                    long lastSeen = cursor.getLong(cursor.getColumnIndex(UserColumns.LAST_SEEN));
                    int sex = cursor.getInt(cursor.getColumnIndex(UserColumns.SEX));
                    String userActivityLine = UserInfoResolveUtil.getUserActivityLine(getContext(), lastSeen, online, sex);

                    if (nonNull(userActivityLine)) {
                        e.onSuccess(userActivityLine);
                    }
                }

                cursor.close();
            }

            e.onComplete();
        });
    }

    @Override
    public Single<Optional<UserEntity>> findUserDboById(int accountId, int ownerId) {
        return Single.create(emitter -> {
            final String where = UserColumns._ID + " = ?";
            final String[] args = new String[]{String.valueOf(ownerId)};
            final Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            UserEntity dbo = null;

            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    dbo = mapUserDbo(cursor);
                }

                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(dbo));
        });
    }

    @Override
    public Single<Optional<CommunityEntity>> findCommunityDboById(int accountId, int ownerId) {
        return Single.create(emitter -> {
            final String where = GroupColumns._ID + " = ?";
            final String[] args = new String[]{String.valueOf(ownerId)};
            final Uri uri = MessengerContentProvider.getGroupsContentUriFor(accountId);

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            CommunityEntity dbo = null;

            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    dbo = mapCommunityDbo(cursor);
                }

                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(dbo));
        });
    }

    @Override
    public Single<Optional<UserEntity>> findUserByDomain(int accoutnId, String domain) {
        return Single.create(emitter -> {
            final Uri uri = MessengerContentProvider.getUserContentUriFor(accoutnId);
            String where = UserColumns.DOMAIN + " LIKE ?";
            String[] args = {domain};
            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            UserEntity entity = null;
            if(nonNull(cursor)){
                if(cursor.moveToNext()){
                    entity = mapUserDbo(cursor);
                }
                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(entity));
        });
    }

    @Override
    public Single<Optional<CommunityEntity>> findCommunityByDomain(int accountId, String domain) {
        return Single.create(emitter -> {
            final Uri uri = MessengerContentProvider.getGroupsContentUriFor(accountId);
            String where = GroupColumns.SCREEN_NAME + " LIKE ?";
            String[] args = {domain};

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            CommunityEntity entity = null;
            if(nonNull(cursor)){
                if(cursor.moveToNext()){
                    entity = mapCommunityDbo(cursor);
                }
                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(entity));
        });
    }

    @Override
    public Single<List<UserEntity>> findUserDbosByIds(int accountId, List<Integer> ids) {
        if (ids.isEmpty()) {
            return Single.just(Collections.emptyList());
        }

        return Single.create(emitter -> {
            final String where;
            final String[] args;
            final Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);

            if (ids.size() == 1) {
                where = UserColumns._ID + " = ?";
                args = new String[]{String.valueOf(ids.get(0))};
            } else {
                where = UserColumns._ID + " IN (" + join(",", ids) + ")";
                args = null;
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null, null);

            List<UserEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapUserDbo(cursor));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Single<List<CommunityEntity>> findCommunityDbosByIds(int accountId, List<Integer> ids) {
        if (ids.isEmpty()) {
            return Single.just(Collections.emptyList());
        }

        return Single.create(emitter -> {
            final String where;
            final String[] args;
            final Uri uri = MessengerContentProvider.getGroupsContentUriFor(accountId);

            if (ids.size() == 1) {
                where = GroupColumns._ID + " = ?";
                args = new String[]{String.valueOf(ids.get(0))};
            } else {
                where = GroupColumns._ID + " IN (" + join(",", ids) + ")";
                args = null;
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null, null);

            List<CommunityEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapCommunityDbo(cursor));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Completable storeUserDbos(int accountId, List<UserEntity> users) {
        return Completable.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(users.size());
            appendUsersInsertOperation(operations, accountId, users);
            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Completable storeCommunityDbos(int accountId, List<CommunityEntity> communityEntities) {
        return Completable.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(communityEntities.size());
            appendCommunitiesInsertOperation(operations, accountId, communityEntities);
            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<Collection<Integer>> getMissingUserIds(int accountId, @NonNull Collection<Integer> ids) {
        return Single.create(e -> {
            if (ids.isEmpty()) {
                e.onSuccess(Collections.emptyList());
                return;
            }

            Set<Integer> copy = new HashSet<>(ids);
            String[] projection = {UserColumns._ID};
            Cursor cursor = getContentResolver().query(MessengerContentProvider.getUserContentUriFor(accountId),
                    projection, UserColumns._ID + " IN ( " + join(",", copy) + ")", null, null);

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(UserColumns._ID));
                    copy.remove(id);
                }

                cursor.close();
            }

            e.onSuccess(copy);
        });
    }

    @Override
    public Single<Collection<Integer>> getMissingCommunityIds(int accountId, @NonNull Collection<Integer> ids) {
        return Single.create(e -> {
            if (ids.isEmpty()) {
                e.onSuccess(Collections.emptyList());
                return;
            }

            Set<Integer> copy = new HashSet<>(ids);
            String[] projection = {GroupColumns._ID};
            Cursor cursor = getContentResolver().query(MessengerContentProvider.getGroupsContentUriFor(accountId),
                    projection, GroupColumns._ID + " IN ( " + join(",", copy) + ")", null, null);

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(GroupColumns._ID));
                    copy.remove(id);
                }

                cursor.close();
            }

            e.onSuccess(copy);
        });
    }

    private static CommunityEntity mapCommunityDbo(Cursor cursor) {
        return new CommunityEntity(cursor.getInt(cursor.getColumnIndex(GroupColumns._ID)))
                .setName(cursor.getString(cursor.getColumnIndex(GroupColumns.NAME)))
                .setScreenName(cursor.getString(cursor.getColumnIndex(GroupColumns.SCREEN_NAME)))
                .setClosed(cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_CLOSED)))
                .setAdmin(cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_ADMIN)) == 1)
                .setAdminLevel(cursor.getInt(cursor.getColumnIndex(GroupColumns.ADMIN_LEVEL)))
                .setMember(cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_MEMBER)) == 1)
                .setType(cursor.getInt(cursor.getColumnIndex(GroupColumns.TYPE)))
                .setPhoto50(cursor.getString(cursor.getColumnIndex(GroupColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndex(GroupColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndex(GroupColumns.PHOTO_200)));
    }

    private static UserEntity mapUserDbo(Cursor cursor) {
        return new UserEntity(cursor.getInt(cursor.getColumnIndex(UserColumns._ID)))
                .setFirstName(cursor.getString(cursor.getColumnIndex(UserColumns.FIRST_NAME)))
                .setLastName(cursor.getString(cursor.getColumnIndex(UserColumns.LAST_NAME)))
                .setOnline(cursor.getInt(cursor.getColumnIndex(UserColumns.ONLINE)) == 1)
                .setOnlineMobile(cursor.getInt(cursor.getColumnIndex(UserColumns.ONLINE_MOBILE)) == 1)
                .setOnlineApp(cursor.getInt(cursor.getColumnIndex(UserColumns.ONLINE_APP)))
                .setPhoto50(cursor.getString(cursor.getColumnIndex(UserColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndex(UserColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndex(UserColumns.PHOTO_200)))
                .setLastSeen(cursor.getLong(cursor.getColumnIndex(UserColumns.LAST_SEEN)))
                .setPlatform(cursor.getInt(cursor.getColumnIndex(UserColumns.PLATFORM)))
                .setStatus(cursor.getString(cursor.getColumnIndex(UserColumns.USER_STATUS)))
                .setSex(cursor.getInt(cursor.getColumnIndex(UserColumns.SEX)))
                .setDomain(cursor.getString(cursor.getColumnIndex(UserColumns.DOMAIN)))
                .setFriend(cursor.getInt(cursor.getColumnIndex(UserColumns.IS_FRIEND)) == 1)
                .setFriendStatus(cursor.getInt(cursor.getColumnIndex(UserColumns.FRIEND_STATUS)));
    }

    private FriendListEntity mapFriendsList(Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(FriendListsColumns.LIST_ID));
        final String name = cursor.getString(cursor.getColumnIndex(FriendListsColumns.NAME));
        return new FriendListEntity(id, name);
    }
}