package biz.dealnote.messenger.model.wrappers;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.model.ISelectable;
import biz.dealnote.messenger.model.Photo;

public class SelectablePhotoWrapper implements Parcelable, Comparable<SelectablePhotoWrapper>, ISelectable {

    private final Photo photo;
    private boolean selected;
    private int index;

    public SelectablePhotoWrapper(@NonNull Photo photo){
        this.photo = photo;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @NonNull
    public Photo getPhoto() {
        return photo;
    }

    protected SelectablePhotoWrapper(Parcel in) {
        photo = in.readParcelable(Photo.class.getClassLoader());
        selected = in.readByte() != 0;
        index = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(photo, flags);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeInt(index);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SelectablePhotoWrapper> CREATOR = new Creator<SelectablePhotoWrapper>() {
        @Override
        public SelectablePhotoWrapper createFromParcel(Parcel in) {
            return new SelectablePhotoWrapper(in);
        }

        @Override
        public SelectablePhotoWrapper[] newArray(int size) {
            return new SelectablePhotoWrapper[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectablePhotoWrapper that = (SelectablePhotoWrapper) o;
        return photo.equals(that.photo);

    }

    @Override
    public int hashCode() {
        return photo.hashCode();
    }

    @Override
    public int compareTo(@NonNull SelectablePhotoWrapper another) {
        return this.index - another.index;
    }
}
