package biz.dealnote.messenger.link.internal;

public class OwnerLink extends AbsInternalLink {

    public int ownerId;

    public OwnerLink(int start, int end, int ownerId, String name) {
        this.start = start;
        this.end = end;
        this.ownerId = ownerId;
        this.targetLine = name;
    }
}
