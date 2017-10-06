package biz.dealnote.messenger.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.adapter.horizontal.Entry;

public class PostFilter implements Entry, Parcelable {

    private int mode;
    private String title;
    private boolean active;
    private int count;

    public PostFilter(int mode, String title) {
        this.mode = mode;
        this.title = title;
    }

    protected PostFilter(Parcel in) {
        mode = in.readInt();
        title = in.readString();
        active = in.readByte() != 0;
        count = in.readInt();
    }

    public static final Creator<PostFilter> CREATOR = new Creator<PostFilter>() {
        @Override
        public PostFilter createFromParcel(Parcel in) {
            return new PostFilter(in);
        }

        @Override
        public PostFilter[] newArray(int size) {
            return new PostFilter[size];
        }
    };

    public String getTitle() {
        return count > 0 ? title + " " + count : title;
    }

    @Override
    public String getTitle(@NonNull Context context) {
        return count > 0 ? title + " " + count : title;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode);
        dest.writeString(title);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeInt(count);
    }

    public int getMode() {
        return mode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
