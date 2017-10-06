package biz.dealnote.messenger.api;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by admin on 21.12.2016.
 * phoenix
 */
public class RetrofitFactory {

    public static final String API_METHOD_URL = "https://api.vk.com/method/";

    private static final RxJava2CallAdapterFactory RX_ADAPTER_FACTORY = RxJava2CallAdapterFactory.create();

    private static final GsonConverterFactory SIMPLE_GSON_CONVERTER_FACTORY
            = GsonConverterFactory.create(new Gson());

    public static Retrofit createCoverartArchiveRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(HttpLogger.DEFAULT_LOGGING_INTERCEPTOR)
                .build();

        return new Retrofit.Builder()
                .baseUrl("http://coverartarchive.org/")
                .addConverterFactory(SIMPLE_GSON_CONVERTER_FACTORY)
                .addCallAdapterFactory(RX_ADAPTER_FACTORY)
                .client(client)
                .build();
    }

    public static Retrofit createMuzicBrainzRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(HttpLogger.DEFAULT_LOGGING_INTERCEPTOR)
                .build();

        return new Retrofit.Builder()
                .baseUrl("http://musicbrainz.org/")
                .addConverterFactory(SIMPLE_GSON_CONVERTER_FACTORY)
                .addCallAdapterFactory(RX_ADAPTER_FACTORY)
                .client(client)
                .build();
    }
}