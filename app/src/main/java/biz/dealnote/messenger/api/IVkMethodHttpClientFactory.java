package biz.dealnote.messenger.api;

import android.support.annotation.Nullable;

import com.google.gson.Gson;

import biz.dealnote.messenger.model.ProxyConfig;
import okhttp3.OkHttpClient;

/**
 * Created by Ruslan Kolbasa on 28.07.2017.
 * phoenix
 */
public interface IVkMethodHttpClientFactory {
    OkHttpClient createDefaultVkHttpClient(int accountId, Gson gson, @Nullable ProxyConfig config);
    OkHttpClient createCustomVkHttpClient(int accountId, String token, Gson gson, @Nullable ProxyConfig config);
    OkHttpClient createServiceVkHttpClient(Gson gson, @Nullable ProxyConfig config);
}