package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 02.01.2017.
 * phoenix
 */
public class PushSettings {

    @SerializedName("sound")
    public int sound;

    @SerializedName("disabled_until")
    public int disabledUntil;
}