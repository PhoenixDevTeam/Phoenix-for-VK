package biz.dealnote.messenger.link.types;

public class WallPostLink extends AbsLink {

    public int ownerId;
    public int postId;

    public WallPostLink(int ownerId, int postId) {
        super(WALL_POST);
        this.ownerId = ownerId;
        this.postId = postId;
    }

    @Override
    public String toString() {
        return "WallPostLink{" +
                "ownerId=" + ownerId +
                ", postId=" + postId +
                '}';
    }
}
