package biz.dealnote.messenger.link.types;

public class AudiosLink extends AbsLink {

    public int ownerId;

    public AudiosLink(int ownerId) {
        super(AUDIOS);
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "AudiosLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
