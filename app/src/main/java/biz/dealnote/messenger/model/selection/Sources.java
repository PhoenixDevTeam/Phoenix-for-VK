package biz.dealnote.messenger.model.selection;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Ruslan Kolbasa on 16.08.2017.
 * phoenix
 */
public class Sources implements Parcelable {

    private final ArrayList<AbsSelectableSource> sources;

    public Sources() {
        this.sources = new ArrayList<>(2);
    }

    protected Sources(Parcel in) {
        int size = in.readInt();
        this.sources = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            @Types
            int type = in.readInt();

            switch (type) {
                case Types.FILES:
                    this.sources.add(in.readParcelable(FileManagerSelectableSource.class.getClassLoader()));
                    break;
                case Types.LOCAL_PHOTOS:
                    this.sources.add(in.readParcelable(LocalPhotosSelectableSource.class.getClassLoader()));
                    break;
                case Types.VK_PHOTOS:
                    this.sources.add(in.readParcelable(VkPhotosSelectableSource.class.getClassLoader()));
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid type " + type);
            }
        }
    }

    public ArrayList<AbsSelectableSource> getSources() {
        return sources;
    }

    public static final Creator<Sources> CREATOR = new Creator<Sources>() {
        @Override
        public Sources createFromParcel(Parcel in) {
            return new Sources(in);
        }

        @Override
        public Sources[] newArray(int size) {
            return new Sources[size];
        }
    };

    public Sources with(AbsSelectableSource source) {
        this.sources.add(source);
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int count(){
        return sources.size();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(sources.size());
        for (AbsSelectableSource source : sources) {
            parcel.writeInt(source.getType());
            parcel.writeParcelable(source, flags);
        }
    }

    public AbsSelectableSource get(int position) {
        return sources.get(position);
    }
}