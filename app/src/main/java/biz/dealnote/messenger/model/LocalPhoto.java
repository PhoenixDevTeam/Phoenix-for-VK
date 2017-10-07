package biz.dealnote.messenger.model;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class LocalPhoto implements Parcelable, Comparable<LocalPhoto>, ISelectable {

    private long imageId;
    private Uri fullImageUri;
    private boolean selected;
    private int index;

    public LocalPhoto() {}

    public static Uri buildUriForPicasso(long id){
        return ContentUris.withAppendedId(Uri.parse("content://media/external/images/media/"), id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(imageId);
        dest.writeString(fullImageUri.toString());
        dest.writeInt(selected ? 1 : 0);
        dest.writeInt(index);
    }

    public LocalPhoto(Parcel in) {
        this.imageId = in.readLong();
        this.fullImageUri = Uri.parse(in.readString());
        this.selected = in.readInt() == 1;
        this.index = in.readInt();
    }

    public static final Creator<LocalPhoto> CREATOR = new Creator<LocalPhoto>() {

        @Override
        public LocalPhoto createFromParcel(Parcel s) {
            return new LocalPhoto(s);
        }

        @Override
        public LocalPhoto[] newArray(int size) {
            return new LocalPhoto[size];
        }
    };

    public long getImageId() {
        return imageId;
    }

    public LocalPhoto setImageId(long imageId) {
        this.imageId = imageId;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public LocalPhoto setIndex(int index) {
        this.index = index;
        return this;
    }

    public LocalPhoto setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public Uri getFullImageUri() {
        return fullImageUri;
    }

    public LocalPhoto setFullImageUri(Uri fullImageUri) {
        this.fullImageUri = fullImageUri;
        return this;
    }

    @Override
    public int compareTo(@NonNull LocalPhoto another) {
        return this.index - another.index;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }
}
