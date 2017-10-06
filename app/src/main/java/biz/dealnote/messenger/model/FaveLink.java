package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 05.12.2016.
 * phoenix
 */
public class FaveLink extends AbsModel implements Parcelable {

    private final String id;

    private String url;

    private String title;

    private String description;

    private String photo50;

    private String photo100;

    public FaveLink(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public FaveLink setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public FaveLink setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FaveLink setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public FaveLink setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public FaveLink setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    protected FaveLink(Parcel in) {
        super(in);
        id = in.readString();
        url = in.readString();
        title = in.readString();
        description = in.readString();
        photo50 = in.readString();
        photo100 = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(photo50);
        dest.writeString(photo100);
    }

    public static final Creator<FaveLink> CREATOR = new Creator<FaveLink>() {
        @Override
        public FaveLink createFromParcel(Parcel in) {
            return new FaveLink(in);
        }

        @Override
        public FaveLink[] newArray(int size) {
            return new FaveLink[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}