package biz.dealnote.messenger.model.criteria;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
public class MessagesCriteria extends Criteria {

    private final int accountId;

    private final int peerId;

    private Integer startMessageId;

    private boolean decryptEncryptedMessages;

    public MessagesCriteria(int accountId, int peerId) {
        this.accountId = accountId;
        this.peerId = peerId;
    }

    public MessagesCriteria setStartMessageId(Integer startMessageId) {
        this.startMessageId = startMessageId;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    @Override
    public String toString() {
        return "MessagesCriteria{" +
                "peerId=" + peerId +
                ", startMessageId=" + startMessageId +
                "} " + super.toString();
    }

    public int getPeerId() {
        return peerId;
    }

    public Integer getStartMessageId() {
        return startMessageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessagesCriteria that = (MessagesCriteria) o;

        return accountId == that.accountId
                && peerId == that.peerId
                && (startMessageId != null ? startMessageId.equals(that.startMessageId) : that.startMessageId == null);
    }

    @Override
    public int hashCode() {
        int result = accountId;
        result = 31 * result + peerId;
        result = 31 * result + (startMessageId != null ? startMessageId.hashCode() : 0);
        return result;
    }

    public boolean isDecryptEncryptedMessages() {
        return decryptEncryptedMessages;
    }

    public MessagesCriteria setDecryptEncryptedMessages(boolean decryptEncryptedMessages) {
        this.decryptEncryptedMessages = decryptEncryptedMessages;
        return this;
    }
}
