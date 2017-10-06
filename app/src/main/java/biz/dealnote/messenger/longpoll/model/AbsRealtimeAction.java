package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 23.11.2016.
 * phoenix
 */
public abstract class AbsRealtimeAction implements Parcelable {

    @RealtimeAction
    private final int action;

    public AbsRealtimeAction(@RealtimeAction int action){
        this.action = action;
    }

    public AbsRealtimeAction(Parcel in) {
        //noinspection ResourceType
        action = in.readInt();
    }

    @RealtimeAction
    public int getAction() {
        return action;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(action);
    }
}
