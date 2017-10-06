package biz.dealnote.messenger.fragment.search.criteria;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.util.ParcelUtils;

/**
 * Created by admin on 28.06.2016.
 * phoenix
 */
public final class MessageSeachCriteria extends BaseSearchCriteria implements Parcelable {

    private Integer peerId;

    public MessageSeachCriteria(String query) {
        super(query);

        // for test
        //appendOption(new SimpleBooleanOption(1, R.string.photo, true));
    }

    private MessageSeachCriteria(Parcel in) {
        super(in);
        this.peerId = ParcelUtils.readObjectInteger(in);
    }

    public MessageSeachCriteria setPeerId(Integer peerId) {
        this.peerId = peerId;
        return this;
    }

    public Integer getPeerId() {
        return peerId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelUtils.writeObjectInteger(dest, peerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MessageSeachCriteria> CREATOR = new Creator<MessageSeachCriteria>() {
        @Override
        public MessageSeachCriteria createFromParcel(Parcel in) {
            return new MessageSeachCriteria(in);
        }

        @Override
        public MessageSeachCriteria[] newArray(int size) {
            return new MessageSeachCriteria[size];
        }
    };
}
