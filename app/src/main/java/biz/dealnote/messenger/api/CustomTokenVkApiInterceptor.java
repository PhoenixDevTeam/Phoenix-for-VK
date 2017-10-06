package biz.dealnote.messenger.api;

import com.google.gson.Gson;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
class CustomTokenVkApiInterceptor extends AbsVkApiInterceptor {

    private final String token;

    CustomTokenVkApiInterceptor(String token, String v, Gson gson) {
        super(v, gson);
        this.token = token;
    }

    @Override
    protected String getToken() {
        return token;
    }
}