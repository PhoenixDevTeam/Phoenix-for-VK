package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 16.07.2017.
 * phoenix
 */
public class LoginResponse {

    // {"error":"need_captcha","captcha_sid":"665120559674","captcha_img":"https:\/\/api.vk.com\/captcha.php?sid=665120559674"}

    @SerializedName("access_token")
    public String access_token;

    @SerializedName("user_id")
    public int user_id;

    @SerializedName("error")
    public String error;

    @SerializedName("error_description")
    public String errorDescription;

    @SerializedName("captcha_sid")
    public String captchaSid;

    @SerializedName("captcha_img")
    public String captchaImg;

    @SerializedName("validation_type")
    public String validationType; // 2fa_sms or 2fa_app

    @SerializedName("phone_mask")
    public String phoneMask;
}