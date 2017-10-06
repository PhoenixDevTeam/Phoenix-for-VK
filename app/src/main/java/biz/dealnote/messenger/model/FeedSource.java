package biz.dealnote.messenger.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import biz.dealnote.messenger.adapter.horizontal.Entry;

public class FeedSource implements Entry, Parcelable {

    private final String value;
    private final Text title;
    private boolean active;

    public FeedSource(String value, String title) {
        this.value = value;
        this.title = new Text(title);
    }

    public FeedSource(String value, @StringRes int title) {
        this.value = value;
        this.title = new Text(title);
    }

    protected FeedSource(Parcel in) {
        value = in.readString();
        title = in.readParcelable(Text.class.getClassLoader());
        active = in.readByte() != 0;
    }

    public String getValue() {
        return value;
    }

    public static final Creator<FeedSource> CREATOR = new Creator<FeedSource>() {
        @Override
        public FeedSource createFromParcel(Parcel in) {
            return new FeedSource(in);
        }

        @Override
        public FeedSource[] newArray(int size) {
            return new FeedSource[size];
        }
    };

    public FeedSource setActive(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(value);
        dest.writeParcelable(title, flags);
        dest.writeByte((byte) (active ? 1 : 0));
    }

    @Override
    public String getTitle(@NonNull Context context) {
        return title.getText(context);
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
