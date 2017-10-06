package biz.dealnote.messenger.link.types;

public class PhotoLink extends AbsLink {

    public int id;
    public int ownerId;

    public PhotoLink(String path) {
        super(PHOTO);
        int dividerPosition = path.indexOf("_");
        this.ownerId = Integer.parseInt(path.substring(0, dividerPosition));
        this.id = Integer.parseInt(path.substring(dividerPosition + 1));
    }

    @Override
    public String toString() {
        return "PhotoLink{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                '}';
    }
}
