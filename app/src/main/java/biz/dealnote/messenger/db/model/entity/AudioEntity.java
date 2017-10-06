package biz.dealnote.messenger.db.model.entity;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class AudioEntity extends Entity {

    private final int id;

    private final int ownerId;

    private String artist;

    private String title;

    private int duration;

    private String url;

    private int lyricsId;

    private int albumId;

    private int genre;

    private String accessKey;

    private boolean deleted;

    public AudioEntity(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getArtist() {
        return artist;
    }

    public AudioEntity setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AudioEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public AudioEntity setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AudioEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getLyricsId() {
        return lyricsId;
    }

    public AudioEntity setLyricsId(int lyricsId) {
        this.lyricsId = lyricsId;
        return this;
    }

    public int getAlbumId() {
        return albumId;
    }

    public AudioEntity setAlbumId(int albumId) {
        this.albumId = albumId;
        return this;
    }

    public int getGenre() {
        return genre;
    }

    public AudioEntity setGenre(int genre) {
        this.genre = genre;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public AudioEntity setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public AudioEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }
}