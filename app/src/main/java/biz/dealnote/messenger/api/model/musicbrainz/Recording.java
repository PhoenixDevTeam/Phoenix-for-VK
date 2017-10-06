package biz.dealnote.messenger.api.model.musicbrainz;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Recording {

    @SerializedName("id")
    public String id;

    @SerializedName("score")
    public int score;

    @SerializedName("title")
    public String title;

    @SerializedName("video")
    public String video;

    @SerializedName("length")
    public long length;

    @SerializedName("releases")
    public List<Release> releases;
}