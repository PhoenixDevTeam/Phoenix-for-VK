package biz.dealnote.messenger.push.message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.longpoll.AppNotificationChannels;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.CommentedType;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.push.NotificationUtils.configOtherPushNotification;

/**
 * Created by ruslan.kolbasa on 10.10.2016.
 * phoenix
 */
public class LikeFCMMessage {

// key: id, value: like_216143660_photo280186075_456239045, class: class java.lang.String
// key: url, value: https://vk.com/photo280186075_456239045?access_key=d7d37c46854499dd3f, class: class java.lang.String
// key: body, value: Emin Guliev liked your photo, class: class java.lang.String
// key: icon, value: like_24, class: class java.lang.String
// key: time, value: 1529682146, class: class java.lang.String
// key: type, value: like, class: class java.lang.String
// key: badge, value: 1, class: class java.lang.String
// key: image, value: [{"width":200,"url":"https:\/\/pp.userapi.com\/c844520\/v844520706\/71a39\/nc5YPeh1yEI.jpg","height":200},{"width":100,"url":"https:\/\/pp.userapi.com\/c844520\/v844520706\/71a3a\/pZLtq6sleHo.jpg","height":100},{"width":50,"url":"https:\/\/pp.userapi.com\/c844520\/v844520706\/71a3b\/qoFJrYXVFdc.jpg","height":50}], class: class java.lang.String
// key: sound, value: 0, class: class java.lang.String
// key: title, value: Notification, class: class java.lang.String
// key: to_id, value: 280186075, class: class java.lang.String
// key: group_id, value: likes, class: class java.lang.String
// key: context, value: {"feedback":true,"item_id":"456239045","owner_id":"280186075","type":"photo"}, class: class java.lang.String

    class LikeContext {
        @SerializedName("feedback")
        int feedback;

        @SerializedName("item_id")
        int item_id;

        @SerializedName("owner_id")
        int owner_id;

        @SerializedName("type")
        String type;

        @SerializedName("reply_id")
        int reply_id;
    }

    private final int accountId;

    private long from;
    private String id;
    private String url;
    private long time;
    private boolean sound;
    private String title;
    private int from_id;
    private String body;
    private int badge;
    private int to_id;
    private String group_id;
    private boolean is_feedback;
    private int item_id;
    private int owner_id;
    private String like_type;
    private int reply_id;

    public LikeFCMMessage(int accountId, RemoteMessage remote) {
        this.accountId = accountId;
        Map<String, String> data = remote.getData();
        this.from = Long.parseLong(remote.getFrom());
        this.id = data.get("id");
        this.url = data.get("url");
        this.time = Long.parseLong(data.get("time"));
        this.sound = Integer.parseInt(data.get("sound")) == 1;
        this.title = data.get("title");
        this.from_id = Integer.parseInt("from_id");
        this.body = data.get("body");
        this.badge = Integer.parseInt(data.get("badge"));
        this.to_id = Integer.parseInt(data.get("to_id"));
        this.group_id = data.get("group_id");

        LikeContext context = new Gson().fromJson(data.get("context"), LikeContext.class);

        this.is_feedback = context.feedback == 1;
        this.item_id = context.item_id;
        this.owner_id = context.owner_id;
        this.like_type = context.type;
        this.reply_id = context.reply_id;
    }

    //todo implement place
    private void notifyImpl(Context context) {
        Place place = null;

        if("post".equals(like_type)){
            place = PlaceFactory.getPostPreviewPlace(accountId, item_id, owner_id, null);
        } else if("photo".equals(like_type)){
            ArrayList<Photo> photos = Utils.singletonArrayList(
                    new Photo().setId(item_id).setOwnerId(owner_id)
            );

            place = PlaceFactory.getSimpleGalleryPlace(accountId, photos, 0, true);
        } else if("video".equals(like_type)){
            place = PlaceFactory.getVideoPreviewPlace(accountId, owner_id, item_id, null);
        } else if("post_comment".equals(like_type)){
            Commented commented = new Commented(item_id, owner_id, CommentedType.POST, null);
            place = PlaceFactory.getCommentsPlace(accountId, commented, reply_id);
        } else if("photo_comment".equals(like_type)){
            Commented commented = new Commented(item_id, owner_id, CommentedType.PHOTO, null);
            place = PlaceFactory.getCommentsPlace(accountId, commented, reply_id);
        } else if("video_comment".equals(like_type)){
            Commented commented = new Commented(item_id, owner_id, CommentedType.VIDEO, null);
            place = PlaceFactory.getCommentsPlace(accountId, commented, reply_id);
        }

        if(place == null){
            return;
        }

//        VkPlace parsedPlace = VkPlace.parse(object);
//
//        if (isNull(parsedPlace)) {
//            PersistentLogger.logThrowable("Push issues", new Exception("LikeFCMMessage, UNKNOWN OBJECT: " + object));
//            return;
//        }
//
//        String userName = (stringEmptyIfNull(firstName) + " " + stringEmptyIfNull(lastName)).trim();
//
//        Place place = null;
//        String contentText = null;
//
//        if (parsedPlace instanceof VkPlace.Photo) {
//            VkPlace.Photo photo = (VkPlace.Photo) parsedPlace;
//
//            ArrayList<Photo> photos = singletonArrayList(
//                    new Photo().setId(photo.getPhotoId()).setOwnerId(photo.getOwnerId())
//            );
//
//            place = PlaceFactory.getSimpleGalleryPlace(accountId, photos, 0, true);
//            contentText = context.getString(R.string.push_user_liked_your_photo, userName);
//        } else if (parsedPlace instanceof VkPlace.PhotoComment) {
//            VkPlace.PhotoComment photoComment = (VkPlace.PhotoComment) parsedPlace;
//            Commented commented = new Commented(photoComment.getPhotoId(), photoComment.getOwnerId(), CommentedType.PHOTO, null);
//            place = PlaceFactory.getCommentsPlace(accountId, commented, replyId);
//            contentText = context.getString(R.string.push_user_liked_your_comment_on_the_photo, userName);
//        } else if (parsedPlace instanceof VkPlace.Video) {
//            VkPlace.Video video = (VkPlace.Video) parsedPlace;
//            place = PlaceFactory.getVideoPreviewPlace(accountId, video.getOwnerId(), video.getVideoId(), null);
//            contentText = context.getString(R.string.push_user_liked_your_video, userName);
//        } else if (parsedPlace instanceof VkPlace.VideoComment) {
//            VkPlace.VideoComment videoComment = (VkPlace.VideoComment) parsedPlace;
//            Commented commented = new Commented(videoComment.getVideoId(), videoComment.getOwnerId(), CommentedType.VIDEO, null);
//            place = PlaceFactory.getCommentsPlace(accountId, commented, replyId);
//            contentText = context.getString(R.string.push_user_liked_your_comment_on_the_video, userName);
//        } else if (parsedPlace instanceof VkPlace.WallPost) {
//            VkPlace.WallPost wallPost = (VkPlace.WallPost) parsedPlace;
//            place = PlaceFactory.getPostPreviewPlace(accountId, wallPost.getPostId(), wallPost.getOwnerId(), null);
//            contentText = context.getString(R.string.push_user_liked_your_post, userName);
//        } else if (parsedPlace instanceof VkPlace.WallComment) {
//            VkPlace.WallComment wallComment = (VkPlace.WallComment) parsedPlace;
//            Commented commented = new Commented(wallComment.getPostId(), wallComment.getOwnerId(), CommentedType.POST, null);
//            place = PlaceFactory.getCommentsPlace(accountId, commented, replyId);
//            contentText = context.getString(R.string.push_user_liked_your_comment_on_the_post, userName);
//        }
//
//        if (isNull(place)) {
//            PersistentLogger.logThrowable("Push issues", new Exception("LikeFCMMessage, UNKNOWN PLACE: " + object));
//            return;
//        }

        final NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()){
            nManager.createNotificationChannel(AppNotificationChannels.getLikesChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.LIKES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_statusbar_like)
                .setContentTitle(context.getString(R.string.like_title))
                .setContentText(title)
                .setNumber(badge)
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        //intent.putExtra(Extra.PLACE, place);
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, from_id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();

        configOtherPushNotification(notification);

        nManager.notify(id, NotificationHelper.NOTIFICATION_LIKE, notification);
    }

    public void notifyIfNeed(Context context) {
        if (!Settings.get()
                .notifications()
                .isLikeNotificationEnable()) {
            return;
        }

        notifyImpl(context);
    }
}