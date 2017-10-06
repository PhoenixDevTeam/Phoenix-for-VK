package biz.dealnote.messenger.fragment.search.options;

import android.os.Parcel;
import android.os.Parcelable;

public class SimpleBooleanOption extends BaseOption implements Parcelable {

    public boolean checked;

    public SimpleBooleanOption(int key, int title, boolean active) {
        super(SIMPLE_BOOLEAN, key, title, active);
    }

    protected SimpleBooleanOption(Parcel in) {
        super(in);
        checked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (checked ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SimpleBooleanOption that = (SimpleBooleanOption) o;

        if (checked != that.checked) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (checked ? 1 : 0);
        return result;
    }

    @Override
    public SimpleBooleanOption clone() throws CloneNotSupportedException {
        return (SimpleBooleanOption) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SimpleBooleanOption> CREATOR = new Creator<SimpleBooleanOption>() {
        @Override
        public SimpleBooleanOption createFromParcel(Parcel in) {
            return new SimpleBooleanOption(in);
        }

        @Override
        public SimpleBooleanOption[] newArray(int size) {
            return new SimpleBooleanOption[size];
        }
    };
}
