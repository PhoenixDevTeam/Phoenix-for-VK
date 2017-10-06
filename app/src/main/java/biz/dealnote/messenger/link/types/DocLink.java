package biz.dealnote.messenger.link.types;

public class DocLink extends AbsLink {

    public int ownerId;
    public int docId;

    public DocLink(int ownerId, int docId) {
        super(DOC);
        this.docId = docId;
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "DocLink{" +
                "ownerId=" + ownerId +
                ", docId=" + docId +
                '}';
    }
}
