package biz.dealnote.messenger.api.impl;

import com.google.gson.Gson;

import biz.dealnote.messenger.api.AuthException;
import biz.dealnote.messenger.api.CaptchaNeedException;
import biz.dealnote.messenger.api.IDirectLoginSeviceProvider;
import biz.dealnote.messenger.api.NeedValidationException;
import biz.dealnote.messenger.api.interfaces.IAuthApi;
import biz.dealnote.messenger.api.model.LoginResponse;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 16.07.2017.
 * phoenix
 */
public class AuthApi implements IAuthApi {

    private final IDirectLoginSeviceProvider service;

    public AuthApi(IDirectLoginSeviceProvider service) {
        this.service = service;
    }

    @Override
    public Single<LoginResponse> directLogin(String grantType, int clientId, String clientSecret,
                                             String username, String pass, String v, boolean twoFaSupported,
                                             String scope, String code, String captchaSid, String captchaKey, boolean forceSms) {
        Integer forceSmsInteger = null;
        if(forceSms){
            forceSmsInteger = 1;
        }

        final Integer finalForceSms = forceSmsInteger;
        return service.provideAuthService()
                .flatMap(service -> service
                        .directLogin(grantType, clientId, clientSecret, username, pass, v, twoFaSupported ? 1 : 0, scope, code, captchaSid, captchaKey, finalForceSms)
                        .compose(withHttpErrorHandling()));
    }

    private static final Gson BASE_RESPONSE_GSON = new Gson();

    private static <T> SingleTransformer<T, T> withHttpErrorHandling() {
        return single -> single.onErrorResumeNext(throwable -> {

            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;

                try {
                    ResponseBody body = httpException.response().errorBody();
                    LoginResponse response = BASE_RESPONSE_GSON.fromJson(body.string(), LoginResponse.class);

                    //{"error":"need_captcha","captcha_sid":"846773809328","captcha_img":"https:\/\/api.vk.com\/captcha.php?sid=846773809328"}

                    if ("need_captcha".equalsIgnoreCase(response.error)) {
                        return Single.error(new CaptchaNeedException(response.captchaSid, response.captchaImg));
                    }

                    if ("need_validation".equalsIgnoreCase(response.error)) {
                        return Single.error(new NeedValidationException(response.validationType));
                    }

                    if(nonEmpty(response.error)){
                        return Single.error(new AuthException(response.error, response.errorDescription));
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }

            return Single.error(throwable);
        });
    }
}