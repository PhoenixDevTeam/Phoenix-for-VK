package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class VkApiDoc implements VKApiAttachment {

    @SerializedName("id")
    public int id;

    @SerializedName("owner_id")
    public int ownerId;

    @SerializedName("title")
    public String title;

    @SerializedName("size")
    public long size;

    @SerializedName("ext")
    public String ext;

    @SerializedName("url")
    public String url;

    @SerializedName("date")
    public long date;

    @SerializedName("type")
    public int type;

    @SerializedName("preview")
    public Preview preview;

    @SerializedName("access_key")
    public String accessKey;

    public static class Preview {

        @SerializedName("photo")
        public Photo photo;

        @SerializedName("video")
        public Video video;

        @SerializedName("graffiti")
        public Graffiti graffiti;

        @SerializedName("audio_msg")
        public AudioMsg audioMsg;
    }

    public static class AudioMsg {

        @SerializedName("duration")
        public int duration;

        @SerializedName("waveform")
        public byte[] waveform;

        @SerializedName("link_ogg")
        public String linkOgg;

        @SerializedName("link_mp3")
        public String linkMp3;
    }

    public static class Graffiti {

        @SerializedName("src")
        public String src;

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;
    }

    public static class Photo {

        @SerializedName("sizes")
        public List<PhotoSizeDto> sizes;
    }

    public static class Video {

        @SerializedName("src")
        public String src;

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;

        @SerializedName("file_size")
        public long fileSize;
    }

    @Override
    public String getType() {
        return VkApiAttachments.TYPE_DOC;
    }
}