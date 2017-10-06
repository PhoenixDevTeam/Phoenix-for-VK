package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admin on 20.04.2017.
 * phoenix
 */
public final class ModelsBundle implements Parcelable, Iterable<AbsModel> {

    private final List<ParcelableModelWrapper> wrappers;

    public ModelsBundle(){
        this.wrappers = new ArrayList<>();
    }

    public ModelsBundle(int capacity){
        this.wrappers = new ArrayList<>(capacity);
    }

    public int size(){
        return wrappers.size();
    }

    public void clear(){
        this.wrappers.clear();
    }

    public ModelsBundle append(AbsModel model){
        this.wrappers.add(ParcelableModelWrapper.wrap(model));
        return this;
    }

    public ModelsBundle append(Collection<? extends AbsModel> data){
        for(AbsModel model : data){
            this.wrappers.add(ParcelableModelWrapper.wrap(model));
        }
        return this;
    }

    private ModelsBundle(Parcel in) {
        wrappers = in.createTypedArrayList(ParcelableModelWrapper.CREATOR);
    }

    public static final Creator<ModelsBundle> CREATOR = new Creator<ModelsBundle>() {
        @Override
        public ModelsBundle createFromParcel(Parcel in) {
            return new ModelsBundle(in);
        }

        @Override
        public ModelsBundle[] newArray(int size) {
            return new ModelsBundle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(wrappers);
    }

    private static class Iter implements Iterator<AbsModel> {

        final Iterator<ParcelableModelWrapper> internal;

        private Iter(Iterator<ParcelableModelWrapper> internal) {
            this.internal = internal;
        }

        @Override
        public boolean hasNext() {
            return internal.hasNext();
        }

        @Override
        public AbsModel next() {
            return internal.next().get();
        }

        @Override
        public void remove() {
            internal.remove();
        }
    }

    @NonNull
    @Override
    public Iterator<AbsModel> iterator() {
        return new Iter(wrappers.iterator());
    }
}
