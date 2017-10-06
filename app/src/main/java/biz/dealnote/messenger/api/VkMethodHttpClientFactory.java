package biz.dealnote.messenger.api;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.model.ProxyConfig;
import okhttp3.OkHttpClient;

/**
 * Created by Ruslan Kolbasa on 28.07.2017.
 * phoenix
 */
public class VkMethodHttpClientFactory implements IVkMethodHttpClientFactory {

    @Override
    public OkHttpClient createDefaultVkHttpClient(int accountId, Gson gson, ProxyConfig config) {
        return createDefaultVkApiOkHttpClient(new DefaultVkApiInterceptor(accountId, ApiVersion.CURRENT, gson), config);
    }

    @Override
    public OkHttpClient createCustomVkHttpClient(int accountId, String token, Gson gson, ProxyConfig config) {
        return createDefaultVkApiOkHttpClient(new CustomTokenVkApiInterceptor(token, ApiVersion.CURRENT, gson), config);
    }

    @Override
    public OkHttpClient createServiceVkHttpClient(Gson gson, ProxyConfig config) {
        return createDefaultVkApiOkHttpClient(new CustomTokenVkApiInterceptor(Constants.SERVICE_TOKEN, ApiVersion.CURRENT, gson), config);
    }

    private OkHttpClient createDefaultVkApiOkHttpClient(AbsVkApiInterceptor interceptor, ProxyConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(HttpLogger.DEFAULT_LOGGING_INTERCEPTOR)
                .readTimeout(25, TimeUnit.SECONDS)
                .connectTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS);

        ProxyUtil.applyProxyConfig(builder, config);
        return builder.build();
    }
}