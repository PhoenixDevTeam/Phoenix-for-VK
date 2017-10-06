package biz.dealnote.messenger.api;

import android.support.annotation.Nullable;

import java.net.InetSocketAddress;
import java.net.Proxy;

import biz.dealnote.messenger.model.ProxyConfig;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public class ProxyUtil {

    public static void applyProxyConfig(OkHttpClient.Builder builder, @Nullable ProxyConfig config){
        if (nonNull(config)) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getAddress(), config.getPort()));

            builder.proxy(proxy);

            if (config.isAuthEnabled()) {
                Authenticator authenticator = (route, response) -> {
                    String credential = Credentials.basic(config.getUser(), config.getPass());
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                };

                builder.proxyAuthenticator(authenticator);
            }
        }
    }
}