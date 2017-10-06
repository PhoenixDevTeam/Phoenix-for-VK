package biz.dealnote.messenger.link.types;

public class OwnerLink extends AbsLink {

    public int ownerId;

    public OwnerLink(int id) {
        super(id > 0 ? PROFILE : GROUP);
        this.ownerId = id;
    }

    @Override
    public String toString() {
        return "OwnerLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
