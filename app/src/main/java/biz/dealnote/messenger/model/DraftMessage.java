package biz.dealnote.messenger.model;

/**
 * Created by ruslan.kolbasa on 07.10.2016.
 * phoenix
 */
public class DraftMessage {

    private int id;

    private String body;

    private int attachmentsCount;

    public DraftMessage(int id, String body) {
        this.body = body;
        this.id = id;
    }

    public int getAttachmentsCount() {
        return attachmentsCount;
    }

    public void setAttachmentsCount(int attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "id=" + getId() + ", body='" + getBody() + '\'' + ", count=" + attachmentsCount;
    }
}
