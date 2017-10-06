package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class Error {

    @SerializedName("error_code")
    public int errorCode;

    @SerializedName("error_msg")
    public String errorMsg;

    @SerializedName("method")
    public String method;

    @SerializedName("captcha_sid")
    public String captchaSid;

    @SerializedName("captcha_img")
    public String captchaImg;

    @SerializedName("redirect_uri")
    public String redirectUri;
}
