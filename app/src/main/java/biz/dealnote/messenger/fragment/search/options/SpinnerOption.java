package biz.dealnote.messenger.fragment.search.options;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class SpinnerOption extends BaseOption implements Parcelable {

    public Entry value;
    public ArrayList<Entry> available;

    public SpinnerOption(int key, int title, boolean active) {
        super(SPINNER, key, title, active);
    }

    public static class Entry implements Parcelable, Cloneable {

        public int id;
        public int name;

        public Entry(int id, int name) {
            this.id = id;
            this.name = name;
        }

        protected Entry(Parcel in) {
            id = in.readInt();
            name = in.readInt();
        }

        @Override
        protected Entry clone() throws CloneNotSupportedException {
            return (Entry) super.clone();
        }

        public static final Creator<Entry> CREATOR = new Creator<Entry>() {
            @Override
            public Entry createFromParcel(Parcel in) {
                return new Entry(in);
            }

            @Override
            public Entry[] newArray(int size) {
                return new Entry[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;
            if (id != entry.id) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeInt(name);
        }
    }

    @Override
    public SpinnerOption clone() throws CloneNotSupportedException {
        SpinnerOption clone = (SpinnerOption) super.clone();
        clone.value = this.value == null ? null : this.value.clone();
        return clone;
    }

    protected SpinnerOption(Parcel in) {
        super(in);
        value = in.readParcelable(Entry.class.getClassLoader());
        available = in.createTypedArrayList(Entry.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(value, flags);
        dest.writeTypedList(available);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SpinnerOption option = (SpinnerOption) o;
        if (value != null ? !value.equals(option.value) : option.value != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SpinnerOption> CREATOR = new Creator<SpinnerOption>() {
        @Override
        public SpinnerOption createFromParcel(Parcel in) {
            return new SpinnerOption(in);
        }

        @Override
        public SpinnerOption[] newArray(int size) {
            return new SpinnerOption[size];
        }
    };

    public String[] createAvailableNames(Context context){
        String[] names = new String[available.size()];
        for(int i = 0; i < available.size(); i++){
            names[i] = context.getString(available.get(i).name);
        }

        return names;
    }
}
