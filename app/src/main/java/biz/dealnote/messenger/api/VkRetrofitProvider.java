package biz.dealnote.messenger.api;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import biz.dealnote.messenger.api.adapters.AttachmentsDtoAdapter;
import biz.dealnote.messenger.api.adapters.AttachmentsEntryDtoAdapter;
import biz.dealnote.messenger.api.adapters.BooleanAdapter;
import biz.dealnote.messenger.api.adapters.ChatDtoAdapter;
import biz.dealnote.messenger.api.adapters.ChatUserDtoAdapter;
import biz.dealnote.messenger.api.adapters.ChatsInfoAdapter;
import biz.dealnote.messenger.api.adapters.CommentDtoAdapter;
import biz.dealnote.messenger.api.adapters.CommunityDtoAdapter;
import biz.dealnote.messenger.api.adapters.CustomCommentsResponseAdapter;
import biz.dealnote.messenger.api.adapters.FeedbackDtoAdapter;
import biz.dealnote.messenger.api.adapters.FeedbackUserArrayDtoAdapter;
import biz.dealnote.messenger.api.adapters.GroupSettingsAdapter;
import biz.dealnote.messenger.api.adapters.LikesListAdapter;
import biz.dealnote.messenger.api.adapters.LongpollUpdateAdapter;
import biz.dealnote.messenger.api.adapters.MessageDtoAdapter;
import biz.dealnote.messenger.api.adapters.NewsAdapter;
import biz.dealnote.messenger.api.adapters.NewsfeedCommentDtoAdapter;
import biz.dealnote.messenger.api.adapters.PhotoAlbumDtoAdapter;
import biz.dealnote.messenger.api.adapters.PhotoDtoAdapter;
import biz.dealnote.messenger.api.adapters.PostDtoAdapter;
import biz.dealnote.messenger.api.adapters.PostSourceDtoAdapter;
import biz.dealnote.messenger.api.adapters.PrivacyDtoAdapter;
import biz.dealnote.messenger.api.adapters.SchoolClazzDtoAdapter;
import biz.dealnote.messenger.api.adapters.SearchDialogsResponseAdapter;
import biz.dealnote.messenger.api.adapters.ToticDtoAdapter;
import biz.dealnote.messenger.api.adapters.UserDtoAdapter;
import biz.dealnote.messenger.api.adapters.VideoDtoAdapter;
import biz.dealnote.messenger.api.model.ChatUserDto;
import biz.dealnote.messenger.api.model.GroupSettingsDto;
import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.api.model.VKApiComment;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.VKApiNews;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPhotoAlbum;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiTopic;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.VkApiAttachments;
import biz.dealnote.messenger.api.model.VkApiPostSource;
import biz.dealnote.messenger.api.model.VkApiPrivacy;
import biz.dealnote.messenger.api.model.database.SchoolClazzDto;
import biz.dealnote.messenger.api.model.feedback.UserArray;
import biz.dealnote.messenger.api.model.feedback.VkApiBaseFeedback;
import biz.dealnote.messenger.api.model.longpoll.AbsLongpollEvent;
import biz.dealnote.messenger.api.model.response.ChatsInfoResponse;
import biz.dealnote.messenger.api.model.response.CustomCommentsResponse;
import biz.dealnote.messenger.api.model.response.LikesListResponse;
import biz.dealnote.messenger.api.model.response.NewsfeedCommentsResponse;
import biz.dealnote.messenger.api.model.response.SearchDialogsResponse;
import biz.dealnote.messenger.settings.IProxySettings;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by Ruslan Kolbasa on 21.07.2017.
 * phoenix
 */
public class VkRetrofitProvider implements IVkRetrofitProvider {

    private static final String API_METHOD_URL = "https://api.vk.com/method/";

    private static final Gson VKGSON = new GsonBuilder()
            .registerTypeAdapter(VkApiAttachments.Entry.class, new AttachmentsEntryDtoAdapter())
            .registerTypeAdapter(VKApiPhoto.class, new PhotoDtoAdapter())
            .registerTypeAdapter(boolean.class, new BooleanAdapter())
            .registerTypeAdapter(VkApiPrivacy.class, new PrivacyDtoAdapter())
            .registerTypeAdapter(VKApiPhotoAlbum.class, new PhotoAlbumDtoAdapter())
            .registerTypeAdapter(VkApiAttachments.class, new AttachmentsDtoAdapter())
            .registerTypeAdapter(VKApiPost.class, new PostDtoAdapter())
            .registerTypeAdapter(VkApiPostSource.class, new PostSourceDtoAdapter())
            .registerTypeAdapter(VKApiUser.class, new UserDtoAdapter())
            .registerTypeAdapter(VKApiCommunity.class, new CommunityDtoAdapter())
            .registerTypeAdapter(VkApiBaseFeedback.class, new FeedbackDtoAdapter())
            .registerTypeAdapter(VKApiComment.class, new CommentDtoAdapter())
            .registerTypeAdapter(VKApiVideo.class, new VideoDtoAdapter())
            .registerTypeAdapter(UserArray.class, new FeedbackUserArrayDtoAdapter())
            .registerTypeAdapter(VKApiMessage.class, new MessageDtoAdapter())
            .registerTypeAdapter(VKApiNews.class, new NewsAdapter())
            .registerTypeAdapter(AbsLongpollEvent.class, new LongpollUpdateAdapter())
            .registerTypeAdapter(ChatsInfoResponse.class, new ChatsInfoAdapter())
            .registerTypeAdapter(VKApiChat.class, new ChatDtoAdapter())
            .registerTypeAdapter(ChatUserDto.class, new ChatUserDtoAdapter())
            .registerTypeAdapter(SchoolClazzDto.class, new SchoolClazzDtoAdapter())
            .registerTypeAdapter(LikesListResponse.class, new LikesListAdapter())
            .registerTypeAdapter(SearchDialogsResponse.class, new SearchDialogsResponseAdapter())
            .registerTypeAdapter(NewsfeedCommentsResponse.Dto.class, new NewsfeedCommentDtoAdapter())
            .registerTypeAdapter(VKApiTopic.class, new ToticDtoAdapter())
            .registerTypeAdapter(GroupSettingsDto.class, new GroupSettingsAdapter())
            .registerTypeAdapter(CustomCommentsResponse.class, new CustomCommentsResponseAdapter())
            .create();

    private static final GsonConverterFactory GSON_CONVERTER_FACTORY = GsonConverterFactory.create(VKGSON);
    private static final RxJava2CallAdapterFactory RX_ADAPTER_FACTORY = RxJava2CallAdapterFactory.create();

    private final IProxySettings proxyManager;
    private final IVkMethodHttpClientFactory clientFactory;

    public VkRetrofitProvider(IProxySettings proxySettings, IVkMethodHttpClientFactory clientFactory) {
        this.proxyManager = proxySettings;
        this.clientFactory = clientFactory;
        this.proxyManager.observeActive()
                .subscribe(optional -> onProxySettingsChanged());
    }

    private void onProxySettingsChanged(){
        synchronized (retrofitCacheLock){
            for(Map.Entry<Integer, RetrofitWrapper> entry : retrofitCache.entrySet()){
                entry.getValue().cleanup();
            }

            retrofitCache.clear();
        }
    }

    @SuppressLint("UseSparseArrays")
    private Map<Integer, RetrofitWrapper> retrofitCache = Collections.synchronizedMap(new HashMap<>(1));

    private final Object retrofitCacheLock = new Object();

    @Override
    public Single<RetrofitWrapper> provideNormalRetrofit(int accountId) {
        return Single.fromCallable(() -> {
            RetrofitWrapper retrofit;

            synchronized (retrofitCacheLock){
                retrofit = retrofitCache.get(accountId);

                if (nonNull(retrofit)) {
                    return retrofit;
                }

                OkHttpClient client = clientFactory.createDefaultVkHttpClient(accountId, VKGSON, proxyManager.getActiveProxy());
                retrofit = createDefaultVkApiRetrofit(client);
                retrofitCache.put(accountId, retrofit);
            }

            return retrofit;
        });
    }

    @Override
    public Single<RetrofitWrapper> provideCustomRetrofit(int accountId, String token) {
        return Single.fromCallable(() -> {
            OkHttpClient client = clientFactory.createCustomVkHttpClient(accountId, token, VKGSON, proxyManager.getActiveProxy());
            return createDefaultVkApiRetrofit(client);
        });
    }

    private volatile RetrofitWrapper serviceRetrofit;

    private final Object serviceRetrofitLock = new Object();

    @Override
    public Single<RetrofitWrapper> provideServiceRetrofit() {
        return Single.fromCallable(() -> {
            if (isNull(serviceRetrofit)) {
                synchronized (serviceRetrofitLock) {
                    if (isNull(serviceRetrofit)) {
                        OkHttpClient client = clientFactory.createServiceVkHttpClient(VKGSON, proxyManager.getActiveProxy());
                        serviceRetrofit = createDefaultVkApiRetrofit(client);
                    }
                }
            }

            return serviceRetrofit;
        });
    }

    @Override
    public Single<OkHttpClient> provideNormalHttpClient(int accountId) {
        return Single.fromCallable(() -> clientFactory.createDefaultVkHttpClient(accountId, VKGSON, proxyManager.getActiveProxy()));
    }

    private RetrofitWrapper createDefaultVkApiRetrofit(OkHttpClient okHttpClient) {
        return RetrofitWrapper.wrap(new Retrofit.Builder()
                .baseUrl(API_METHOD_URL)
                .addConverterFactory(GSON_CONVERTER_FACTORY)
                .addCallAdapterFactory(RX_ADAPTER_FACTORY)
                .client(okHttpClient)
                .build());
    }
}