package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.LoginResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 16.07.2017.
 * phoenix
 */
public interface IAuthService {

    @FormUrlEncoded
    @POST("token")
    Single<LoginResponse> directLogin(@Field("grant_type") String grantType,
                                      @Field("client_id") int clientId,
                                      @Field("client_secret") String clientSecret,
                                      @Field("username") String username,
                                      @Field("password") String password,
                                      @Field("v") String v,
                                      @Field("2fa_supported") int twoFaSupported,
                                      @Field("scope") String scope,
                                      @Field("code") String smscode,
                                      @Field("captcha_sid") String captchaSid,
                                      @Field("captcha_key") String captchaKey,
                                      @Field("force_sms") Integer forceSms);

}