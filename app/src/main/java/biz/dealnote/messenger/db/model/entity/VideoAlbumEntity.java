package biz.dealnote.messenger.db.model.entity;

/**
 * Created by Ruslan Kolbasa on 20.09.2017.
 * phoenix
 */
public class VideoAlbumEntity extends Entity {

    private final int id;

    private final int ownerId;

    private String title;

    private String photo160;

    private String photo320;

    private int count;

    private long updateTime;

    private PrivacyEntity privacy;

    public VideoAlbumEntity(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public VideoAlbumEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPhoto160() {
        return photo160;
    }

    public VideoAlbumEntity setPhoto160(String photo160) {
        this.photo160 = photo160;
        return this;
    }

    public String getPhoto320() {
        return photo320;
    }

    public VideoAlbumEntity setPhoto320(String photo320) {
        this.photo320 = photo320;
        return this;
    }

    public int getCount() {
        return count;
    }

    public VideoAlbumEntity setCount(int count) {
        this.count = count;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public VideoAlbumEntity setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public PrivacyEntity getPrivacy() {
        return privacy;
    }

    public VideoAlbumEntity setPrivacy(PrivacyEntity privacy) {
        this.privacy = privacy;
        return this;
    }
}