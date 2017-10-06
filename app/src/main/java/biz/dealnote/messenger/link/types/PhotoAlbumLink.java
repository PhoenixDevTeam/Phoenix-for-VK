package biz.dealnote.messenger.link.types;

public class PhotoAlbumLink extends AbsLink {

    public int ownerId;
    public int albumId;

    public PhotoAlbumLink(int ownerId, int albumId) {
        super(PHOTO_ALBUM);
        this.ownerId = ownerId;
        this.albumId = albumId;
    }

    @Override
    public String toString() {
        return "PhotoAlbumLink{" +
                "ownerId=" + ownerId +
                ", albumId=" + albumId +
                '}';
    }
}
