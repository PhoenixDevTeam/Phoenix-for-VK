package biz.dealnote.messenger.media.gif;

import android.support.annotation.NonNull;

import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by admin on 13.08.2017.
 * phoenix
 */
public class AppGifPlayerFactory implements IGifPlayerFactory {

    private final IProxySettings proxySettings;

    public AppGifPlayerFactory(IProxySettings proxySettings) {
        this.proxySettings = proxySettings;
    }

    @Override
    public IGifPlayer createGifPlayer(@NonNull String url) {
        ProxyConfig config = proxySettings.getActiveProxy();

        if(Objects.nonNull(config)){
            return new ExoGifPlayer(url, config);
        } else {
            return new DefaultGifPlayer(url);
        }
    }
}