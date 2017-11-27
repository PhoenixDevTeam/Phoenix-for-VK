package biz.dealnote.messenger;

import android.content.Context;

import biz.dealnote.messenger.api.CaptchaProvider;
import biz.dealnote.messenger.api.ICaptchaProvider;
import biz.dealnote.messenger.api.impl.Networker;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.db.impl.AppStores;
import biz.dealnote.messenger.db.impl.LogsStore;
import biz.dealnote.messenger.db.interfaces.ILogsStore;
import biz.dealnote.messenger.db.interfaces.IStores;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.domain.IBlacklistRepository;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.domain.impl.AttachmentsRepository;
import biz.dealnote.messenger.domain.impl.BlacklistRepository;
import biz.dealnote.messenger.domain.impl.WallsImpl;
import biz.dealnote.messenger.media.gif.AppGifPlayerFactory;
import biz.dealnote.messenger.media.gif.IGifPlayerFactory;
import biz.dealnote.messenger.media.voice.IVoicePlayerFactory;
import biz.dealnote.messenger.media.voice.VoicePlayerFactory;
import biz.dealnote.messenger.push.GcmTokenProvider;
import biz.dealnote.messenger.push.IDevideIdProvider;
import biz.dealnote.messenger.push.IGcmTokenProvider;
import biz.dealnote.messenger.push.IPushRegistrationResolver;
import biz.dealnote.messenger.push.PushRegistrationResolver;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.ProxySettingsImpl;
import biz.dealnote.messenger.settings.SettingsImpl;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static biz.dealnote.messenger.util.Objects.isNull;

/**
 * Created by ruslan.kolbasa on 01.12.2016.
 * phoenix
 */
public class Injection {

    private static volatile ICaptchaProvider captchaProvider;

    private static IProxySettings proxySettings = new ProxySettingsImpl(provideApplicationContext());

    public static IProxySettings provideProxySettings(){
        return proxySettings;
    }

    public static IGifPlayerFactory provideGifPlayerFactory(){
        return new AppGifPlayerFactory(proxySettings);
    }

    private static volatile IPushRegistrationResolver resolver;

    public static IVoicePlayerFactory provideVoicePlayerFactory(){
        return new VoicePlayerFactory(provideApplicationContext(), provideProxySettings());
    }

    public static IPushRegistrationResolver providePushRegistrationResolver(){
        if(isNull(resolver)){
            synchronized (Injection.class){
                if(isNull(resolver)){
                    final Context context = provideApplicationContext();
                    final IDevideIdProvider devideIdProvider = () -> Utils.getDiviceId(context);
                    final IGcmTokenProvider tokenProvider = new GcmTokenProvider(context);
                    resolver = new PushRegistrationResolver(tokenProvider, devideIdProvider, provideSettings(), provideNetworkInterfaces());
                }
            }
        }

        return resolver;
    }

    public static ICaptchaProvider provideCaptchaProvider() {
        if(isNull(captchaProvider)){
            synchronized (Injection.class){
                if(isNull(captchaProvider)){
                    captchaProvider = new CaptchaProvider(provideApplicationContext(), provideMainThreadScheduler());
                }
            }
        }
        return captchaProvider;
    }

    private static volatile IAttachmentsRepository attachmentsRepository;

    public static IAttachmentsRepository provideAttachmentsRepository(){
        if(isNull(attachmentsRepository)){
            synchronized (Injection.class){
                if(isNull(attachmentsRepository)){
                    attachmentsRepository = new AttachmentsRepository(provideStores().attachments(), InteractorFactory.createOwnerInteractor());
                }
            }
        }

        return attachmentsRepository;
    }

    private static volatile IWalls walls;

    public static IWalls provideWalls(){
        if(isNull(walls)){
            synchronized (Injection.class){
                if(isNull(walls)){
                    walls = new WallsImpl(provideNetworkInterfaces(), provideStores());
                }
            }
        }

        return walls;
    }

    private static INetworker networkerInstance = new Networker(proxySettings);

    public static INetworker provideNetworkInterfaces(){
        return networkerInstance;
    }

    public static IStores provideStores(){
        return AppStores.getInstance(App.getInstance());
    }

    private static volatile IBlacklistRepository blacklistRepository;

    public static IBlacklistRepository provideBlacklistRepository() {
        if(isNull(blacklistRepository)){
            synchronized (Injection.class){
                if(isNull(blacklistRepository)){
                    blacklistRepository = new BlacklistRepository();
                }
            }
        }
        return blacklistRepository;
    }

    public static ISettings provideSettings(){
        return SettingsImpl.getInstance(App.getInstance());
    }

    private static volatile ILogsStore logsStore;

    public static ILogsStore provideLogsStore(){
        if(isNull(logsStore)){
            synchronized (Injection.class){
                if(isNull(logsStore)){
                    logsStore = new LogsStore(provideApplicationContext());
                }
            }
        }
        return logsStore;
    }

    public static Scheduler provideMainThreadScheduler(){
        return AndroidSchedulers.mainThread();
    }

    public static Context provideApplicationContext() {
        return App.getInstance();
    }
}