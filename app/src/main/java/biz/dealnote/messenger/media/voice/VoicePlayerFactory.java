package biz.dealnote.messenger.media.voice;

import android.content.Context;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by r.kolbasa on 27.11.2017.
 * Phoenix-for-VK
 */
public class VoicePlayerFactory implements IVoicePlayerFactory {

    private final Context app;
    private final IProxySettings proxySettings;

    public VoicePlayerFactory(Context context, IProxySettings proxySettings) {
        this.app = context.getApplicationContext();
        this.proxySettings = proxySettings;
    }

    @NonNull
    @Override
    public IVoicePlayer createPlayer() {
        ProxyConfig config = proxySettings.getActiveProxy();

        if(Objects.isNull(config)){
            return new DefaultVoicePlayer();
        } else {
            return new ExoVoicePlayer(app, config);
        }
    }
}