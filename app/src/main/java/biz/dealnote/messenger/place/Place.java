package biz.dealnote.messenger.place;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class Place implements Parcelable {

    public static final int VIDEO_PREVIEW = 1;
    public static final int FRIENDS_AND_FOLLOWERS = 2;
    public static final int WIKI_PAGE = 3;
    public static final int EXTERNAL_LINK = 4;
    public static final int DOC_PREVIEW = 5;
    public static final int WALL_POST = 6;
    public static final int COMMENTS = 7;
    public static final int WALL = 8;
    public static final int CONVERSATION_ATTACHMENTS = 9;
    public static final int PLAYER = 10;
    public static final int SEARCH = 11;
    public static final int CHAT = 12;
    public static final int BUILD_NEW_POST = 13;
    public static final int EDIT_COMMENT = 15;
    public static final int EDIT_POST = 16;
    public static final int REPOST = 17;
    public static final int DIALOGS = 18;
    public static final int FORWARD_MESSAGES = 19;
    public static final int TOPICS = 20;
    public static final int CHAT_MEMBERS = 21;
    public static final int COMMUNITIES = 23;
    public static final int LIKES_AND_COPIES = 25;
    public static final int VIDEO_ALBUM = 26;
    public static final int AUDIOS = 27;
    public static final int VIDEOS = 28;
    public static final int VK_PHOTO_ALBUMS = 29;
    public static final int VK_PHOTO_ALBUM = 30;
    public static final int VK_PHOTO_ALBUM_GALLERY = 31;

    public static final int FAVE_PHOTOS_GALLERY = 32;
    public static final int SIMPLE_PHOTO_GALLERY = 33;
    public static final int POLL = 34;
    public static final int PREFERENCES = 35;
    public static final int DOCS = 36;
    public static final int FEED = 37;
    public static final int NOTIFICATIONS = 38;
    public static final int BOOKMARKS = 39;
    public static final int RESOLVE_DOMAIN = 40;
    public static final int VK_INTERNAL_PLAYER = 41;
    public static final int NOTIFICATION_SETTINGS = 42;
    public static final int CREATE_PHOTO_ALBUM = 43;
    public static final int EDIT_PHOTO_ALBUM = 45;
    public static final int MESSAGE_LOOKUP = 46;
    public static final int AUDIO_CURRENT_PLAYLIST = 48;
    public static final int GIF_PAGER = 49;
    public static final int SECURITY = 50;
    public static final int CREATE_POLL = 51;
    public static final int COMMENT_CREATE = 52;
    public static final int LOGS = 53;
    public static final int LOCAL_IMAGE_ALBUM = 54;
    public static final int SINGLE_SEARCH = 55;
    public static final int NEWSFEED_COMMENTS = 56;
    public static final int COMMUNITY_CONTROL = 57;
    public static final int COMMUNITY_BAN_EDIT = 58;
    public static final int COMMUNITY_ADD_BAN = 59;

    public static final int VK_PHOTO_TMP_SOURCE = 60;

    public static final int COMMUNITY_MANAGER_EDIT = 61;
    public static final int COMMUNITY_MANAGER_ADD = 62;

    public static final int REQUEST_EXECUTOR = 63;
    public static final int USER_BLACKLIST = 64;

    public static final int PROXY_ADD = 65;
    public static final int DRAWER_EDIT = 66;

    public int type;
    private Bundle args;
    public Fragment target;
    public int requestCode;

    public Place(int type) {
        this.type = type;
    }

    protected Place(Parcel in) {
        type = in.readInt();
        args = in.readBundle(getClass().getClassLoader());
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public void tryOpenWith(@NonNull Context context){
        if(context instanceof PlaceProvider){
            ((PlaceProvider)context).openPlace(this);
        }
    }

    public Place targetTo(Fragment fragment, int requestCode){
        this.target = fragment;
        this.requestCode = requestCode;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeBundle(args);
    }

    public Bundle getArgs() {
        return args;
    }

    public Place setArguments(Bundle arguments) {
        this.args = arguments;
        return this;
    }

    public Place withStringExtra(String name, String value){
        prepareArguments().putString(name, value);
        return this;
    }

    public Place withParcelableExtra(String name, Parcelable parcelableExtra){
        prepareArguments().putParcelable(name, parcelableExtra);
        return this;
    }

    public Place withIntExtra(String name, int value){
        prepareArguments().putInt(name, value);
        return this;
    }

    public Bundle prepareArguments(){
        if(args == null){
            args = new Bundle();
        }

        return args;
    }

    public boolean hasTargeting(){
        return target != null;
    }

    public void applyTargetingTo(@NonNull Fragment fragment){
        if(hasTargeting()){
            fragment.setTargetFragment(target, requestCode);
        }
    }

    public int getType() {
        return type;
    }
}
