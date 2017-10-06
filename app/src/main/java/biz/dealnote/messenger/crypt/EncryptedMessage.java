package biz.dealnote.messenger.crypt;

/**
 * Created by admin on 04.09.2017.
 * phoenix
 */
public class EncryptedMessage {

    private final long sessionId;
    private final String originalBody;

    @KeyLocationPolicy
    private final int keyLocationPolicy;

    public EncryptedMessage(long sessionId, String originalBody, @KeyLocationPolicy int keyLocationPolicy) {
        this.sessionId = sessionId;
        this.originalBody = originalBody;
        this.keyLocationPolicy = keyLocationPolicy;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getOriginalBody() {
        return originalBody;
    }

    @KeyLocationPolicy
    public int getKeyLocationPolicy() {
        return keyLocationPolicy;
    }
}