package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import java.util.Collection;

import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiAudio;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IAudioApi {

    @CheckResult
    Single<int[]> setBroadcast(IdPair audio, Collection<Integer> targetIds);

    @CheckResult
    Single<Items<VKApiAudio>> search(String query, Boolean autoComplete, Boolean lyrics,
                                     Boolean performerOnly, Integer sort, Boolean searchOwn,
                                     Integer offset, Integer count);

    @CheckResult
    Single<VKApiAudio> restore(int audioId, Integer ownerId);

    @CheckResult
    Single<Boolean> delete(int audioId, int ownerId);

    @CheckResult
    Single<Integer> add(int audioId, int ownerId, Integer groupId, Integer album_id);

    @CheckResult
    Single<Items<VKApiAudio>> get(Integer ownerId, Integer albumId, Collection<Integer> audioIds,
                                  Integer offset, Integer count);

}
