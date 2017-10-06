package biz.dealnote.messenger.api.impl;

import java.io.IOException;
import java.util.Map;

import biz.dealnote.messenger.api.IVkRetrofitProvider;
import biz.dealnote.messenger.api.RetrofitFactory;
import biz.dealnote.messenger.api.interfaces.IOtherApi;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Optional;
import io.reactivex.Single;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public class OtherApi implements IOtherApi {

    private final IVkRetrofitProvider provider;

    private final int accountId;

    public OtherApi(int accountId, IVkRetrofitProvider provider) {
        this.provider = provider;
        this.accountId = accountId;
    }

    @Override
    public Single<Optional<String>> rawRequest(String method, Map<String, String> postParams) {
        final FormBody.Builder bodyBuilder = new FormBody.Builder();

        for (Map.Entry<String, String> entry : postParams.entrySet()) {
            bodyBuilder.add(entry.getKey(), entry.getValue());
        }

        return provider.provideNormalHttpClient(accountId)
                .flatMap(client -> Single
                        .<Response>create(emitter -> {
                            Request request = new Request.Builder()
                                    .url(RetrofitFactory.API_METHOD_URL + method)
                                    .method("POST", bodyBuilder.build())
                                    .build();

                            Call call = client.newCall(request);

                            emitter.setCancellable(call::cancel);

                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    emitter.onError(e);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    emitter.onSuccess(response);
                                }
                            });
                        }))
                .map(response -> {
                    ResponseBody body = response.body();
                    String responseBodyString = Objects.nonNull(body) ? body.string() : null;
                    return Optional.wrap(responseBodyString);
                });
    }
}