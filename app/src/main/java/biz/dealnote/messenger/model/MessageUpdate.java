package biz.dealnote.messenger.model;

/**
 * Created by admin on 15.11.2016.
 * phoenix
 */
public class MessageUpdate {

    private final int accountId;

    private final int messageId;

    private StatusUpdate statusUpdate;

    private SentUpdate sentUpdate;

    public MessageUpdate setSentUpdate(SentUpdate sentUpdate) {
        this.sentUpdate = sentUpdate;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    public SentUpdate getSentUpdate() {
        return sentUpdate;
    }

    public MessageUpdate(int accountId, int messageId) {
        this.accountId = accountId;
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public StatusUpdate getStatusUpdate() {
        return statusUpdate;
    }

    public MessageUpdate setStatusUpdate(StatusUpdate statusUpdate) {
        this.statusUpdate = statusUpdate;
        return this;
    }

    public static class StatusUpdate {

        @MessageStatus
        private final int status;

        public StatusUpdate(@MessageStatus int status) {
            this.status = status;
        }

        @MessageStatus
        public int getStatus() {
            return status;
        }
    }

    public static class SentUpdate {

        private final int vkid;

        public SentUpdate(int vkid) {
            this.vkid = vkid;
        }

        public int getVkid() {
            return vkid;
        }
    }
}
