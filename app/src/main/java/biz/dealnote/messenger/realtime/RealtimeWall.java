package biz.dealnote.messenger.realtime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.api.model.VKApiPost;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Created by admin on 30.06.2017.
 * phoenix
 */
public class RealtimeWall {

    private final List<IdPair> focused = new LinkedList<>();

    private static final int MAX_FOCUS_COUNT = 5;

    public void put(int postId, int ownerId) {
        if (focused.size() >= MAX_FOCUS_COUNT) {
            focused.remove(0);
        }

        focused.add(new IdPair(postId, ownerId));
    }

    public Flowable<List<VKApiPost>> observe(final int accountId) {
        return createRefreshSingle(accountId, focused)
                .repeat();
    }

    private static Single<List<VKApiPost>> createRefreshSingle(int accountId, List<IdPair> focused) {
        return Single.just(new Object())
                .delay(5, TimeUnit.SECONDS)
                .flatMap(o -> {
                    if(focused.isEmpty()){
                        return Single.just(Collections.<VKApiPost>emptyList());
                    }

                    return Apis.get()
                            .vkDefault(accountId)
                            .wall()
                            .getById(focused, true, 0, null)
                            .map(response -> response.posts);
                })
                .onErrorReturn(throwable -> Collections.emptyList());
    }
}