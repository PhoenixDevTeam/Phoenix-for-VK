package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

public class IdPair implements Parcelable {

    public int id;
    public int ownerId;

    public IdPair(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    protected IdPair(Parcel in) {
        id = in.readInt();
        ownerId = in.readInt();
    }

    public static final Creator<IdPair> CREATOR = new Creator<IdPair>() {
        @Override
        public IdPair createFromParcel(Parcel in) {
            return new IdPair(in);
        }

        @Override
        public IdPair[] newArray(int size) {
            return new IdPair[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(ownerId);
    }

    public boolean isValid(){
        return id != 0 && ownerId != 0;
    }

    @Override
    public String toString() {
        return "IdPair{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                '}';
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getId() {
        return id;
    }
}
