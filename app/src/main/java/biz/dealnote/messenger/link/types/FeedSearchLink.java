package biz.dealnote.messenger.link.types;

/**
 * Created by r.kolbasa on 18.12.2017.
 * Phoenix-for-VK
 */
public class FeedSearchLink extends AbsLink {

    private final String q;

    public FeedSearchLink(String q) {
        super(FEED_SEARCH);
        this.q = q;
    }

    public String getQ() {
        return q;
    }
}