package biz.dealnote.messenger.crypt;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import biz.dealnote.messenger.util.ParcelUtils;

/**
 * Created by admin on 19.10.2016.
 * phoenix
 */
public class ExchangeMessage implements Parcelable {

    @SerializedName("v")
    private int version;

    @SerializedName("sid")
    private long sessionId;

    @SerializedName("public_key")
    private String publicKey;

    @SerializedName("aes_key")
    private String aesKey;

    @SessionState
    @SerializedName("session_state")
    private int senderSessionState;

    @SerializedName("error_code")
    private int errorCode;

    @KeyLocationPolicy
    @SerializedName("klp")
    private Integer keyLocationPolicy;

    private ExchangeMessage(Builder builder) {
        this.publicKey = builder.publicKey;
        this.version = builder.version;
        this.sessionId = builder.sessionId;
        this.aesKey = builder.aesKey;
        this.senderSessionState = builder.sessionState;
        this.errorCode = builder.errorCode;
        this.keyLocationPolicy = builder.keyLocationPolicy;
    }

    protected ExchangeMessage(Parcel in) {
        version = in.readInt();
        sessionId = in.readLong();
        publicKey = in.readString();
        aesKey = in.readString();

        @SessionState
        int s = in.readInt();
        senderSessionState = s;
        errorCode = in.readInt();

        @KeyLocationPolicy
        Integer klp = ParcelUtils.readObjectInteger(in);
        keyLocationPolicy = klp;
    }

    public boolean isError(){
        return errorCode != 0;
    }

    public static final Creator<ExchangeMessage> CREATOR = new Creator<ExchangeMessage>() {
        @Override
        public ExchangeMessage createFromParcel(Parcel in) {
            return new ExchangeMessage(in);
        }

        @Override
        public ExchangeMessage[] newArray(int size) {
            return new ExchangeMessage[size];
        }
    };

    @Override
    public String toString() {
        return "RSA" + new Gson().toJson(this);
    }

    public int getVersion() {
        return version;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getAesKey() {
        return aesKey;
    }

    @SessionState
    public int getSenderSessionState() {
        return senderSessionState;
    }

    @KeyLocationPolicy
    public int getKeyLocationPolicy() {
        return keyLocationPolicy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(version);
        dest.writeLong(sessionId);
        dest.writeString(publicKey);
        dest.writeString(aesKey);
        dest.writeInt(senderSessionState);
        dest.writeInt(errorCode);
        ParcelUtils.writeObjectInteger(dest, keyLocationPolicy);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static final class Builder {

        private String publicKey;
        private String aesKey;
        private final int version;
        private final long sessionId;

        @KeyLocationPolicy
        private Integer keyLocationPolicy;

        @SessionState
        private final int sessionState;

        private int errorCode;

        public Builder(int version, long sessionId, @SessionState int senderSessionState) {
            this.version = version;
            this.sessionId = sessionId;
            this.sessionState = senderSessionState;
        }

        public Builder setPublicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder setAesKey(String aesKey) {
            this.aesKey = aesKey;
            return this;
        }

        public Builder setErrorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ExchangeMessage create() {
            return new ExchangeMessage(this);
        }

        public Builder setKeyLocationPolicy(@KeyLocationPolicy int keyLocationPolicy) {
            this.keyLocationPolicy = keyLocationPolicy;
            return this;
        }
    }

}
