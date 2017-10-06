package biz.dealnote.messenger.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class StringArray implements Parcelable {

    private final String[] array;

    public StringArray(@NonNull String[] orig) {
        this.array = orig;
    }

    public StringArray(@NonNull List<String> orig){
        this.array = orig.toArray(new String[orig.size()]);
    }

    protected StringArray(Parcel in) {
        array = in.createStringArray();
    }

    public static final Creator<StringArray> CREATOR = new Creator<StringArray>() {
        @Override
        public StringArray createFromParcel(Parcel in) {
            return new StringArray(in);
        }

        @Override
        public StringArray[] newArray(int size) {
            return new StringArray[size];
        }
    };

    public String[] getArray() {
        return array;
    }

    public List<String> asList(){
        return Arrays.asList(array);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(array);
    }
}
