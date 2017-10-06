package biz.dealnote.messenger.api.model.musicbrainz;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecordingSearchResult {

    @SerializedName("created")
    public String created;

    @SerializedName("count")
    public int count;

    @SerializedName("offset")
    public int offset;

    @SerializedName("recordings")
    public List<Recording> recordings;
}