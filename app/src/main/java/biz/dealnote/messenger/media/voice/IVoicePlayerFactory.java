package biz.dealnote.messenger.media.voice;

import android.support.annotation.NonNull;

/**
 * Created by r.kolbasa on 27.11.2017.
 * Phoenix-for-VK
 */
public interface IVoicePlayerFactory {
    @NonNull
    IVoicePlayer createPlayer();
}