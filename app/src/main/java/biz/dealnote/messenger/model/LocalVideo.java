package biz.dealnote.messenger.model;

import android.net.Uri;

/**
 * Created by ruslan.kolbasa on 20.12.2016.
 * phoenix
 */
public class LocalVideo {

    private final long id;
    private final Uri data;

    private long size;

    private long duration;

    private String title;

    public LocalVideo(long id, Uri data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public Uri getData() {
        return data;
    }

    public long getSize() {
        return size;
    }

    public LocalVideo setSize(long size) {
        this.size = size;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public LocalVideo setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public LocalVideo setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public String toString() {
        return "LocalVideo{" +
                "id=" + id +
                ", data=" + data +
                ", size=" + size +
                ", duration=" + duration +
                ", title='" + title + '\'' +
                '}';
    }
}
