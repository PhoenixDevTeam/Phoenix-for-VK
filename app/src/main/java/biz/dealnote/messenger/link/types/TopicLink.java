package biz.dealnote.messenger.link.types;

public class TopicLink extends AbsLink {

    public int ownerId;
    public int topicId;

    public TopicLink(int topicId, int ownerId) {
        super(TOPIC);
        this.topicId = topicId;
        this.ownerId = -Math.abs(ownerId); // group only
    }

    @Override
    public String toString() {
        return "TopicLink{" +
                "ownerId=" + ownerId +
                ", topicId=" + topicId +
                '}';
    }
}
