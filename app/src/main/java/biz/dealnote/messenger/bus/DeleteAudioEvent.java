package biz.dealnote.messenger.bus;

public class DeleteAudioEvent {

    public int id;
    public int ownerId;
    public boolean deleted;

    public DeleteAudioEvent(int id, int ownerId, boolean deleted) {
        this.id = id;
        this.ownerId = ownerId;
        this.deleted = deleted;
    }
}
