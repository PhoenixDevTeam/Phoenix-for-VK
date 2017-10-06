package biz.dealnote.messenger.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.StatFs;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.task.LocalPhotoRequestHandler;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Objects;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

/**
 * Created by Ruslan Kolbasa on 28.07.2017.
 * phoenix
 */
public class PicassoInstance {

    private static final String TAG = PicassoInstance.class.getSimpleName();

    private final IProxySettings proxySettings;

    private final Context app;

    private PicassoInstance(Context app, IProxySettings proxySettings) {
        this.app = app;
        this.proxySettings = proxySettings;
        this.proxySettings.observeActive()
                .subscribe(ignored -> onProxyChanged());
    }

    private void onProxyChanged() {
        synchronized (this) {
            if(Objects.nonNull(this.singleton)){
                this.singleton.shutdown();
                this.singleton = null;
            }

            Logger.d(TAG, "Picasso singleton shutdown");
        }
    }

    private static PicassoInstance instance;

    public static void init(Context context, IProxySettings proxySettings) {
        instance = new PicassoInstance(context.getApplicationContext(), proxySettings);
    }

    private volatile Picasso singleton;

    private Picasso getSingleton() {
        if (Objects.isNull(singleton)) {
            synchronized (this) {
                if (Objects.isNull(singleton)) {
                    singleton = create();
                }
            }
        }


        return singleton;
    }

    public static Picasso with() {
        return instance.getSingleton();
    }

    private Picasso create() {
        Logger.d(TAG, "Picasso singleton creation");

        File cache = new File(app.getCacheDir(), "picasso-cache");

        if (!cache.exists()) {
            cache.mkdirs();
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(cache, calculateDiskCacheSize(cache)));

        ProxyConfig config = proxySettings.getActiveProxy();

        if (Objects.nonNull(config)) {
            Authenticator authenticator = null;
            if (config.isAuthEnabled()) {
                authenticator = (route, response) -> {
                    String credential = Credentials.basic(config.getUser(), config.getPass());
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                };
            }

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getAddress(), config.getPort()));
            builder.proxy(proxy);

            if (Objects.nonNull(authenticator)) {
                builder.proxyAuthenticator(authenticator);
            }
        }

        OkHttp3Downloader downloader = new OkHttp3Downloader(builder.build());

        return new Picasso.Builder(app)
                .downloader(downloader)
                .addRequestHandler(new LocalPhotoRequestHandler(app))
                .defaultBitmapConfig(Bitmap.Config.RGB_565)
                .build();

        //Picasso.setSingletonInstance(picasso);
    }

    // from picasso sources
    private static long calculateDiskCacheSize(File dir) {
        long size = 5242880L;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long blockCount = Build.VERSION.SDK_INT < 18 ? (long) statFs.getBlockCount() : statFs.getBlockCountLong();
            long blockSize = Build.VERSION.SDK_INT < 18 ? (long) statFs.getBlockSize() : statFs.getBlockSizeLong();
            long available = blockCount * blockSize;
            size = available / 50L;
        } catch (IllegalArgumentException ignored) {

        }

        return Math.max(Math.min(size, 52428800L), 5242880L);
    }
}