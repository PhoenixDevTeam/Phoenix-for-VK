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

    private ImportantUpdate importantUpdate;

    private DeleteUpdate deleteUpdate;

    public void setDeleteUpdate(DeleteUpdate deleteUpdate) {
        this.deleteUpdate = deleteUpdate;
    }

    public void setImportantUpdate(ImportantUpdate importantUpdate) {
        this.importantUpdate = importantUpdate;
    }

    public DeleteUpdate getDeleteUpdate() {
        return deleteUpdate;
    }

    public ImportantUpdate getImportantUpdate() {
        return importantUpdate;
    }

    public void setSentUpdate(SentUpdate sentUpdate) {
        this.sentUpdate = sentUpdate;
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

    public void setStatusUpdate(StatusUpdate statusUpdate) {
        this.statusUpdate = statusUpdate;
    }

    public static class ImportantUpdate {
        private final boolean important;

        public ImportantUpdate(boolean important) {
            this.important = important;
        }

        public boolean isImportant() {
            return important;
        }
    }

    public static class DeleteUpdate {

        private final boolean deleted;

        public DeleteUpdate(boolean deleted) {
            this.deleted = deleted;
        }

        public boolean isDeleted() {
            return deleted;
        }
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
