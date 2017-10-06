package biz.dealnote.messenger.api.model.musicbrainz;

import com.google.gson.annotations.SerializedName;

public class Release {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("status")
    public String status;

    @SerializedName("date")
    public String date;

    @SerializedName("country")
    public String country;
}