package biz.dealnote.messenger.link.types;

public class VideoLink extends AbsLink {

    public int ownerId;
    public int videoId;

    public VideoLink(int ownerId, int videoId) {
        super(VIDEO);
        this.videoId = videoId;
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "VideoLink{" +
                "ownerId=" + ownerId +
                ", videoId=" + videoId +
                '}';
    }
}
