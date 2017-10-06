package biz.dealnote.messenger.api;

import android.os.SystemClock;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.model.Captcha;
import biz.dealnote.messenger.api.model.Error;
import biz.dealnote.messenger.api.model.response.VkReponse;
import biz.dealnote.messenger.service.ApiErrorCodes;
import biz.dealnote.messenger.util.Exestime;
import biz.dealnote.messenger.util.Logger;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 21.12.2016.
 * phoenix
 */
abstract class AbsVkApiInterceptor implements Interceptor {

    private static final String TAG = AbsVkApiInterceptor.class.getSimpleName();

    private final String version;
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private final Gson gson;

    AbsVkApiInterceptor(String version, Gson gson) {
        this.version = version;
        this.gson = gson;
    }

    protected abstract String getToken();

    private static final Random RANDOM = new Random();

    @Override
    public Response intercept(Chain chain) throws IOException {
        int requestId = COUNTER.incrementAndGet();

        Request original = chain.request();

        String token = getToken();

        FormBody.Builder formBuiler = new FormBody.Builder()
                .add("v", version);

        if(nonEmpty(token)){
            formBuiler.add("access_token", token);
        }

        RequestBody body = original.body();

        if(body instanceof FormBody){
            FormBody formBody = (FormBody) body;

            for(int i = 0; i < formBody.size(); i++){
                String name = formBody.name(i);
                String value = formBody.value(i);
                formBuiler.add(name, value);
            }
        }

        Request request = original.newBuilder()
                .method("POST", formBuiler.build())
                .build();

        Response response;
        ResponseBody responseBody;
        String responseBodyString;

        int tryCounter = 0;

        while (true) {
            tryCounter++;

            Logger.d(TAG, "Requestid: " + requestId + ", try to get response, try: " + tryCounter);

            long startGet = System.currentTimeMillis();

            response = chain.proceed(request);
            responseBody = response.body();
            responseBodyString = responseBody.string();

            Exestime.log("AbsVkApiInterceptor.get", startGet);

            long startParse = System.currentTimeMillis();
            VkReponse vkReponse = gson.fromJson(responseBodyString, VkReponse.class);
            //Logger.d(TAG, "vkReponse: " + vkReponse);

            Exestime.log("AbsVkApiInterceptor.parse", startParse);

            Error error = isNull(vkReponse) ? null : vkReponse.error;

            if (nonNull(error)) {
                if (error.errorCode == ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND) {
                    synchronized (AbsVkApiInterceptor.class) {
                        int sleepMs = 1000 + RANDOM.nextInt(500);

                        Logger.d(TAG, "Requestid: " + requestId + ", try sleep for " + sleepMs + " ms");

                        try {
                            Thread.sleep(sleepMs);
                        } catch (InterruptedException ignored) {
                        }
                    }

                    continue;
                }

                if(error.errorCode == ApiErrorCodes.CAPTCHA_NEED){
                    Captcha captcha = new Captcha(error.captchaSid, error.captchaImg);

                    ICaptchaProvider provider = Injection.provideCaptchaProvider();
                    provider.requestCaptha(captcha.getSid(), captcha);

                    Logger.d(TAG, "CAPTCHA need, lookup started");

                    String code = null;

                    while (true){
                        try {
                            code = provider.lookupCode(captcha.getSid());

                            if(nonNull(code)){
                                Logger.d(TAG, "CAPTCHA found: " + code);
                                break;
                            } else {
                                Logger.d(TAG, "CAPTCHA not found yet");
                                SystemClock.sleep(1000);
                            }
                        } catch (OutOfDateException e) {
                            Logger.d(TAG, "CAPTCHA out of date!!!");
                            break;
                        }
                    }

                    if(nonNull(code)){
                        formBuiler.add("captcha_sid", captcha.getSid());
                        formBuiler.add("captcha_key", code);

                        request = original.newBuilder()
                                .method("POST", formBuiler.build())
                                .build();
                        continue;
                    }
                }
            }

            break;
        }

        Response.Builder builder = response.newBuilder()
                .body(ResponseBody.create(responseBody.contentType(), responseBodyString));

        if (isNull(token)) {
            builder.code(401);
        }

        return builder.build();
    }
}