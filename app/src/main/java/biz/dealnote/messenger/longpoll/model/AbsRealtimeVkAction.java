package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class AbsRealtimeVkAction extends AbsRealtimeAction implements Parcelable {

    private int accountId;

    public AbsRealtimeVkAction(Parcel in) {
        super(in);
        accountId = in.readInt();
    }

    public int getAccountId() {
        return accountId;
    }

    public AbsRealtimeVkAction(int accountId, @RealtimeAction int action) {
        super(action);
        this.accountId = accountId;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(accountId);
    }
}
