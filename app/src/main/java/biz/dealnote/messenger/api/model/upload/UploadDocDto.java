package biz.dealnote.messenger.api.model.upload;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class UploadDocDto {

    @SerializedName("file")
    public String file;

    @Override
    public String toString() {
        return "UploadDocDto{" +
                "file='" + file + '\'' +
                '}';
    }
}
