package biz.dealnote.messenger.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.VkApiPrivacy;

import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.join;

public class Privacy implements Parcelable, Cloneable {

    private int type;
    private ArrayList<User> allowedUsers;
    private ArrayList<User> disallowedUsers;
    private ArrayList<FriendList> allowedLists;
    private ArrayList<FriendList> disallowedLists;

    public Privacy(int type) {
        this.type = type;
        this.allowedUsers = new ArrayList<>();
        this.disallowedUsers = new ArrayList<>();
        this.allowedLists = new ArrayList<>();
        this.disallowedLists = new ArrayList<>();
    }

    public Privacy() {
        this(VkApiPrivacy.Type.ALL);
    }

    protected Privacy(Parcel in) {
        this.type = in.readInt();
        this.allowedUsers = in.createTypedArrayList(User.CREATOR);
        this.disallowedUsers = in.createTypedArrayList(User.CREATOR);
        this.allowedLists = in.createTypedArrayList(FriendList.CREATOR);
        this.disallowedLists = in.createTypedArrayList(FriendList.CREATOR);
    }

    public static final Creator<Privacy> CREATOR = new Creator<Privacy>() {
        @Override
        public Privacy createFromParcel(Parcel in) {
            return new Privacy(in);
        }

        @Override
        public Privacy[] newArray(int size) {
            return new Privacy[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeTypedList(allowedUsers);
        dest.writeTypedList(disallowedUsers);
        dest.writeTypedList(allowedLists);
        dest.writeTypedList(disallowedLists);
    }

    public int getType() {
        return type;
    }

    public Privacy setType(int type) {
        this.type = type;
        return this;
    }

    public List<User> getAllowedUsers() {
        return Collections.unmodifiableList(allowedUsers);
    }

    public List<User> getDisallowedUsers() {
        return Collections.unmodifiableList(disallowedUsers);
    }

    public List<FriendList> getAllowedLists() {
        return Collections.unmodifiableList(allowedLists);
    }

    public List<FriendList> getDisallowedLists() {
        return Collections.unmodifiableList(disallowedLists);
    }

    public Privacy allowFor(User user) {
        if (!this.allowedUsers.contains(user)) {
            this.allowedUsers.add(user);
        }

        return this;
    }

    public Privacy disallowFor(User user) {
        if (!this.disallowedUsers.contains(user)) {
            this.disallowedUsers.add(user);
        }

        return this;
    }

    public Privacy allowFor(FriendList friendList) {
        if (!this.allowedLists.contains(friendList)) {
            this.allowedLists.add(friendList);
        }

        return this;
    }

    public Privacy disallowFor(FriendList friendList) {
        if (!this.disallowedLists.contains(friendList)) {
            this.disallowedLists.add(friendList);
        }

        return this;
    }

    public void removeFromAllowed(@NonNull User user){
        this.allowedUsers.remove(user);
    }

    public void removeFromAllowed(@NonNull FriendList friendList){
        this.allowedLists.remove(friendList);
    }

    public void removeFromDisallowed(@NonNull User user){
        this.disallowedUsers.remove(user);
    }

    public void removeFromDisallowed(@NonNull FriendList friendList){
        this.disallowedLists.remove(friendList);
    }

    public String createAllowedString(Context context) {
        String sufix;
        switch (type) {
            default:
                sufix = context.getString(R.string.privacy_to_all_users);
                break;
            case VkApiPrivacy.Type.FRIENDS:
                sufix = context.getString(R.string.privacy_to_friends_only);
                break;
            case VkApiPrivacy.Type.FRIENDS_OF_FRIENDS:
                sufix = context.getString(R.string.privacy_to_friends_and_friends_of_friends);
                break;
            case VkApiPrivacy.Type.ONLY_ME:
                sufix = context.getString(R.string.privacy_to_only_me);
                break;
        }

        String users = join(", ", allowedUsers);
        String friendsLists = join(", ", allowedLists);
        String additional = isEmpty(users) ? friendsLists : (isEmpty(friendsLists) ? users : users + ", " + friendsLists);
        String and = context.getString(R.string.and);
        return isEmpty(additional) ? sufix : sufix + " " + and + " " + additional;
    }

    public String createDisallowedString(){
        String users = join(", ", disallowedUsers);
        String friendsLists = join(", ", disallowedLists);
        String additional = isEmpty(users) ? friendsLists : (isEmpty(friendsLists) ? users : users + ", " + friendsLists);
        return isEmpty(additional) ? "-" : additional;
    }

    @Override
    public Privacy clone() throws CloneNotSupportedException {
        Privacy clone = (Privacy) super.clone();
        clone.allowedUsers = new ArrayList<>(this.allowedUsers);
        clone.allowedLists = new ArrayList<>(this.allowedLists);
        clone.disallowedUsers = new ArrayList<>(this.disallowedUsers);
        clone.disallowedLists = new ArrayList<>(this.disallowedLists);
        return clone;
    }
}