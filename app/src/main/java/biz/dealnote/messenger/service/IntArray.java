package biz.dealnote.messenger.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntArray implements Parcelable {

    private int[] ids;

    public int[] getIds() {
        return ids;
    }

    public IntArray(int ... value){
        this.ids = value;
    }

    public IntArray(@NonNull List<Integer> ids) {
        this.ids = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            this.ids[i] = ids.get(i);
        }
    }

    public IntArray(int singleElement){
        this.ids = new int[]{singleElement};
    }

    @NonNull
    public List<Integer> asList() {
        List<Integer> tmp = new ArrayList<>();
        for (int i : ids) {
            tmp.add(i);
        }

        return tmp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(ids);
    }

    public IntArray(Parcel in) {
        ids = in.createIntArray();
    }

    public static final Creator<IntArray> CREATOR = new Creator<IntArray>() {

        @Override
        public IntArray createFromParcel(Parcel s) {
            return new IntArray(s);
        }

        @Override
        public IntArray[] newArray(int size) {
            return new IntArray[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntArray intArray1 = (IntArray) o;
        return Arrays.equals(ids, intArray1.ids);

    }

    public boolean isEmpty(){
        return ids == null || ids.length == 0;
    }

    @Override
    public int hashCode() {
        return ids != null ? Arrays.hashCode(ids) : 0;
    }

    @Override
    public String toString() {
        return "IntArray{" +
                "ids=" + Arrays.toString(ids) +
                '}';
    }
}
