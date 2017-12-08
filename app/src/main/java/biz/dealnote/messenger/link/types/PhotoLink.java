package biz.dealnote.messenger.link.types;

public class PhotoLink extends AbsLink {

    public final int id;
    public final int ownerId;

    public PhotoLink(int id, int ownerId) {
        super(PHOTO);
        this.id = id;
        this.ownerId = ownerId;
    }
}