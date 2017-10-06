package biz.dealnote.messenger.link.types;

public class WallLink extends AbsLink {

    public int ownerId;

    public WallLink(int ownerId) {
        super(WALL);
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "WallLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
