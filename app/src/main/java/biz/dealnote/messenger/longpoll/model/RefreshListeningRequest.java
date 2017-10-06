package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 23.11.2016.
 * phoenix
 */
public class RefreshListeningRequest extends AbsRealtimeAction implements Parcelable {

    public RefreshListeningRequest(){
        super(RealtimeAction.KEEP_LISTENING_REQUEST);
    }

    protected RefreshListeningRequest(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<RefreshListeningRequest> CREATOR = new Creator<RefreshListeningRequest>() {
        @Override
        public RefreshListeningRequest createFromParcel(Parcel in) {
            return new RefreshListeningRequest(in);
        }

        @Override
        public RefreshListeningRequest[] newArray(int size) {
            return new RefreshListeningRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}