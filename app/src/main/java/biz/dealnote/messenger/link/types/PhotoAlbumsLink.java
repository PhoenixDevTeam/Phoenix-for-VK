package biz.dealnote.messenger.link.types;

public class PhotoAlbumsLink extends AbsLink {

    public int ownerId;

    public PhotoAlbumsLink(int ownerId) {
        super(ALBUMS);
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "PhotoAlbumsLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
