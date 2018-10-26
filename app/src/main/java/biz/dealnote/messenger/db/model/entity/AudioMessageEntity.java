package biz.dealnote.messenger.db.model.entity;

public class AudioMessageEntity extends Entity {

    private final int id;

    private final int ownerId;

    private int duration;

    private byte[] waveform;

    private String linkOgg;

    private String linkMp3;

    private String accessKey;

    public AudioMessageEntity(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getDuration() {
        return duration;
    }

    public AudioMessageEntity setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public byte[] getWaveform() {
        return waveform;
    }

    public AudioMessageEntity setWaveform(byte[] waveform) {
        this.waveform = waveform;
        return this;
    }

    public String getLinkOgg() {
        return linkOgg;
    }

    public AudioMessageEntity setLinkOgg(String linkOgg) {
        this.linkOgg = linkOgg;
        return this;
    }

    public String getLinkMp3() {
        return linkMp3;
    }

    public AudioMessageEntity setLinkMp3(String linkMp3) {
        this.linkMp3 = linkMp3;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public AudioMessageEntity setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }
}