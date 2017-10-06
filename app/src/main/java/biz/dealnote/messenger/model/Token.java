package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ruslan Kolbasa on 14.06.2017.
 * phoenix
 */
public final class Token implements Parcelable {

    private final int ownerId;

    private final String accessToken;

    public Token(int ownerId, String accessToken) {
        this.ownerId = ownerId;
        this.accessToken = accessToken;
    }

    private Token(Parcel in) {
        ownerId = in.readInt();
        accessToken = in.readString();
    }

    public static final Creator<Token> CREATOR = new Creator<Token>() {
        @Override
        public Token createFromParcel(Parcel in) {
            return new Token(in);
        }

        @Override
        public Token[] newArray(int size) {
            return new Token[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ownerId);
        dest.writeString(accessToken);
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return "Token{" +
                "ownerId=" + ownerId +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}