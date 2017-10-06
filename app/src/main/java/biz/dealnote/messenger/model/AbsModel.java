package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;

/**
 * Created by admin on 22.11.2016.
 * phoenix
 */
public abstract class AbsModel implements Parcelable {

    public AbsModel(){

    }

    @SuppressWarnings("unused")
    public AbsModel(Parcel in) {

    }

    @CallSuper
    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}