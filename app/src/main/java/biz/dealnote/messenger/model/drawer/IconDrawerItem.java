package biz.dealnote.messenger.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class IconDrawerItem extends SectionDrawerItem implements Parcelable {

    private int icon;

    public IconDrawerItem(int section, int icon, int title) {
        super(TYPE_WITH_ICON, section, title);
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(icon);
    }

    public IconDrawerItem(Parcel in) {
        super(in);
        this.icon = in.readInt();
    }

    public static Creator<IconDrawerItem> CREATOR = new Creator<IconDrawerItem>() {
        public IconDrawerItem createFromParcel(Parcel source) {
            return new IconDrawerItem(source);
        }

        public IconDrawerItem[] newArray(int size) {
            return new IconDrawerItem[size];
        }
    };
}
