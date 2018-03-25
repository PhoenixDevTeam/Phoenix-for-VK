package biz.dealnote.messenger.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class RecentChat extends AbsDrawerItem implements Parcelable {

    private int aid;
    private int peerId;
    private String title;
    private String iconUrl;

    public RecentChat(int aid, int peerId, String title, String iconUrl) {
        super(TYPE_RECENT_CHAT);
        this.aid = aid;
        this.peerId = peerId;
        this.title = title;
        this.iconUrl = iconUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getAid() {
        return aid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RecentChat that = (RecentChat) o;
        return aid == that.aid && peerId == that.peerId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + aid;
        result = 31 * result + peerId;
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(aid);
        dest.writeInt(peerId);
        dest.writeString(title);
        dest.writeString(iconUrl);
    }

    public RecentChat(Parcel in) {
        super(in);
        this.aid = in.readInt();
        this.peerId = in.readInt();
        this.title = in.readString();
        this.iconUrl = in.readString();
    }

    public static Creator<RecentChat> CREATOR = new Creator<RecentChat>() {
        public RecentChat createFromParcel(Parcel source) {
            return new RecentChat(source);
        }

        public RecentChat[] newArray(int size) {
            return new RecentChat[size];
        }
    };
}
