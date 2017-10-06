package biz.dealnote.messenger.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class SectionDrawerItem extends AbsDrawerItem implements Parcelable {

    private int section;
    private int title;
    private int count;

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public SectionDrawerItem(int type, int section, int title) {
        super(type);
        this.section = section;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SectionDrawerItem that = (SectionDrawerItem) o;
        return section == that.section;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + section;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(section);
        dest.writeInt(title);
        dest.writeInt(count);
    }

    public SectionDrawerItem(Parcel in) {
        super(in);
        this.section = in.readInt();
        this.title = in.readInt();
        this.count = in.readInt();
    }

    public static Creator<SectionDrawerItem> CREATOR = new Creator<SectionDrawerItem>() {
        public SectionDrawerItem createFromParcel(Parcel source) {
            return new SectionDrawerItem(source);
        }

        public SectionDrawerItem[] newArray(int size) {
            return new SectionDrawerItem[size];
        }
    };
}
