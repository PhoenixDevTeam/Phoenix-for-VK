package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

import biz.dealnote.messenger.util.ParcelUtils;

/**
 * Created by ruslan.kolbasa on 14.12.2016.
 * phoenix
 */
public final class Icon implements Parcelable {

    @DrawableRes
    private final Integer res;

    private final String url;

    private Icon(Integer res, String url) {
        this.res = res;
        this.url = url;
    }

    public static Icon fromUrl(String url) {
        return new Icon(null, url);
    }

    public static Icon fromResources(@DrawableRes int res) {
        return new Icon(res, null);
    }

    private Icon(Parcel in) {
        res = ParcelUtils.readObjectInteger(in);
        url = in.readString();
    }

    public static final Creator<Icon> CREATOR = new Creator<Icon>() {
        @Override
        public Icon createFromParcel(Parcel in) {
            return new Icon(in);
        }

        @Override
        public Icon[] newArray(int size) {
            return new Icon[size];
        }
    };

    public boolean isRemote() {
        return url != null;
    }

    public Integer getRes() {
        return res;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeObjectInteger(dest, res);
        dest.writeString(url);
    }
}