package biz.dealnote.messenger;

import android.content.Context;

import biz.dealnote.messenger.api.CaptchaProvider;
import biz.dealnote.messenger.api.ICaptchaProvider;
import biz.dealnote.messenger.api.impl.Networker;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.db.impl.AppRepositories;
import biz.dealnote.messenger.db.impl.LogsStore;
import biz.dealnote.messenger.db.interfaces.ILogsStore;
import biz.dealnote.messenger.db.interfaces.IRepositories;
import biz.dealnote.messenger.interactor.IAttachmentsRepository;
import biz.dealnote.messenger.interactor.IDialogsInteractor;
import biz.dealnote.messenger.interactor.INewsfeedInteractor;
import biz.dealnote.messenger.interactor.IOwnersInteractor;
import biz.dealnote.messenger.interactor.IStickersInteractor;
import biz.dealnote.messenger.interactor.IWalls;
import biz.dealnote.messenger.interactor.InteractorFactory;
import biz.dealnote.messenger.interactor.impl.AttachmentsRepository;
import biz.dealnote.messenger.interactor.impl.DialogsInteractor;
import biz.dealnote.messenger.interactor.impl.NewsfeedInteractor;
import biz.dealnote.messenger.interactor.impl.OwnersInteractor;
import biz.dealnote.messenger.interactor.impl.StickersInteractor;
import biz.dealnote.messenger.interactor.impl.WallsImpl;
import biz.dealnote.messenger.media.gif.AppGifPlayerFactory;
import biz.dealnote.messenger.media.gif.IGifPlayerFactory;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.ProxySettingsImpl;
import biz.dealnote.messenger.settings.SettingsImpl;
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
                    attachmentsRepository = new AttachmentsRepository(provideRepositories().attachments(), InteractorFactory.createOwnerInteractor());
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
                    walls = new WallsImpl(provideNetworkInterfaces(), provideRepositories());
                }
            }
        }

        return walls;
    }

    private static INetworker networkerInstance = new Networker(proxySettings);

    public static INetworker provideNetworkInterfaces(){
        return networkerInstance;
    }

    public static IRepositories provideRepositories(){
        return AppRepositories.getInstance(App.getInstance());
    }

    public static ISettings provideSettings(){
        return SettingsImpl.getInstance(App.getInstance());
    }

    public static IDialogsInteractor provideDialogsInteractor(){
        return new DialogsInteractor(provideNetworkInterfaces(), provideRepositories());
    }

    public static IStickersInteractor provideStickersInteractor(){
        return new StickersInteractor(provideNetworkInterfaces(), provideRepositories().stickers());
    }

    public static IOwnersInteractor provideOwnersInteractor(){
        return new OwnersInteractor(provideNetworkInterfaces(), provideRepositories().owners());
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

    public static INewsfeedInteractor provideNewsfeedInteractor(){
        return new NewsfeedInteractor(provideNetworkInterfaces(), provideOwnersInteractor());
    }
}