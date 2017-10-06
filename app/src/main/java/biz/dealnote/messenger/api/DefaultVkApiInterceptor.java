package biz.dealnote.messenger.api;

import com.google.gson.Gson;

import biz.dealnote.messenger.settings.Settings;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class DefaultVkApiInterceptor extends AbsVkApiInterceptor {

    private final int accountId;

    DefaultVkApiInterceptor(int accountId, String v, Gson gson) {
        super(v, gson);
        this.accountId = accountId;
    }

    @Override
    protected String getToken() {
        return Settings.get()
                .accounts()
                .getAccessToken(accountId);
    }
}