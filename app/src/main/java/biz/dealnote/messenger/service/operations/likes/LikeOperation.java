package biz.dealnote.messenger.service.operations.likes;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.interfaces.ILikesApi;
import biz.dealnote.messenger.api.model.VKApiAttachment;
import biz.dealnote.messenger.db.column.NewsColumns;
import biz.dealnote.messenger.db.column.PhotosColumns;
import biz.dealnote.messenger.db.column.VideoColumns;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class LikeOperation extends AbsApiOperation {

    public static final String EXTRA_ADD = "add";

    public static final String RESULT_LIKE_COUNT = "like_count";
    public static final String RESULT_USER_LIKES = "user_likes";

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        boolean add = request.getBoolean(EXTRA_ADD);
        int ownerId = request.getInt(Extra.OWNER_ID);
        int itemId = request.getInt(Extra.ID);
        String type = request.getString(Extra.TYPE);
        String accessKey = request.getString(Extra.ACCESS_KEY);
        boolean storeToDb = request.getBoolean(Extra.STORE_TO_DB);

        ILikesApi likesApi = Apis.get()
                .vkDefault(accountId)
                .likes();

        int resultCount;
        if (add) {
            resultCount = likesApi.add(type, ownerId, itemId, accessKey)
                    .blockingGet();
        } else {
            resultCount = likesApi.delete(type, ownerId, itemId)
                    .blockingGet();
        }

        Bundle result = new Bundle();
        result.putInt(RESULT_LIKE_COUNT, resultCount);
        result.putBoolean(RESULT_USER_LIKES, add);

        if (storeToDb) {
            save(context, accountId, add, ownerId, itemId, type, resultCount);
        }

        return result;
    }

    private void save(Context context, int aid, boolean userLikes, int ownerId, int itemId, String type, int resultCount) {
        switch (type) {
            // посты и комментарии сюдой не лайкаются !!!

            //case "post":
            //    savePostLikes(context, aid, ownerId, itemId, resultCount, userLikes);
            //    break;
            //case "comment":
            //    saveCommentLikes(context, aid, ownerId, itemId, resultCount, userLikes);
            //    break;

            case VKApiAttachment.TYPE_PHOTO:
                savePhotoLikes(context, aid, itemId, ownerId, resultCount, userLikes);
                break;
            case VKApiAttachment.TYPE_VIDEO:
                saveVideoLikes(context, aid, itemId, ownerId, resultCount, userLikes);
                break;
        }
    }

    private void savePhotoLikes(Context context, int aid, int photoId, int ownerId, int count, boolean userLikes) {
        ContentValues cv = new ContentValues();
        cv.put(PhotosColumns.USER_LIKES, userLikes);
        cv.put(PhotosColumns.LIKES, count);
        context.getContentResolver().update(MessengerContentProvider.getPhotosContentUriFor(aid), cv,
                PhotosColumns.PHOTO_ID + " = ? AND " + PhotosColumns.OWNER_ID + " = ?",
                new String[]{String.valueOf(photoId), String.valueOf(ownerId)});
    }

    private void saveVideoLikes(Context context, int aid, int videoId, int ownerId, int count, boolean userLikes) {
        ContentValues cv = new ContentValues();
        cv.put(VideoColumns.USER_LIKES, userLikes);
        cv.put(VideoColumns.LIKES, count);
        context.getContentResolver().update(MessengerContentProvider.getVideosContentUriFor(aid), cv,
                VideoColumns.VIDEO_ID + " = ? AND " + VideoColumns.OWNER_ID + " = ?",
                new String[]{String.valueOf(videoId), String.valueOf(ownerId)});
    }

    private void savePostLikes(Context context, int aid, int ownerId, int postId, int count, boolean userLikes) {
        // посты
        //Repositories.getInstance()
        //        .wall()
        //        .update(aid, ownerId, postId, new PostPatch().withLikes(count, userLikes))
         //       .blockingAwait();

        //ContentValues contentValues = new ContentValues();
        //contentValues.put(PostsColumns.USER_LIKES, userLikes);
        //contentValues.put(PostsColumns.LIKES_COUNT, count);
        //context.getContentResolver().update(MessengerContentProvider.getPostsContentUriFor(aid), contentValues,
        //        PostsColumns.POST_ID + " = ? AND " + PostsColumns.OWNER_ID + " = ?",
        //        new String[]{String.valueOf(postId), String.valueOf(ownerId)});

        // новости
        ContentValues cv = new ContentValues();
        cv.put(NewsColumns.USER_LIKE, userLikes);
        cv.put(NewsColumns.LIKE_COUNT, count);
        context.getContentResolver().update(MessengerContentProvider.getNewsContentUriFor(aid), cv,
                NewsColumns.POST_ID + " = ? AND " + NewsColumns.SOURCE_ID + " = ?",
                new String[]{String.valueOf(postId), String.valueOf(ownerId)});
    }
}
