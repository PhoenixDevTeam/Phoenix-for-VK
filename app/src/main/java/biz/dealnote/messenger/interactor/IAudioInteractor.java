package biz.dealnote.messenger.interactor;

import java.util.Collection;

import biz.dealnote.messenger.model.Audio;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.annotations.Nullable;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public interface IAudioInteractor {
    Single<Audio> add(int accountId, Audio audio, Integer groupId, Integer albumId);
    Completable delete(int accountId, int audioId, int ownerId);
    Completable restore(int accountId, int audioId, int ownerId);

    Completable sendBroadcast(int accountId, int audioOwnerId, int audioId, @Nullable Collection<Integer> targetIds);
}