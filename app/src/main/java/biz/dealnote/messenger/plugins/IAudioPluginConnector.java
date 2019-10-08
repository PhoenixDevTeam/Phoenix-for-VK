package biz.dealnote.messenger.plugins;

import java.util.List;

import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.IdPair;
import io.reactivex.Single;

/**
 * Created by admin on 2/3/2018.
 * Phoenix-for-VK
 */
public interface IAudioPluginConnector {
    Single<List<Audio>> get(int ownerId, int offset);

    Single<List<Audio>> getById(List<IdPair> audios);

    Single<List<Audio>> getPopular(int foreign, int genre);
    Single<List<Audio>> search(String query, boolean own, int offset);
    boolean isPluginAvailable();
}