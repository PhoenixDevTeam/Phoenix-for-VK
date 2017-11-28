package biz.dealnote.messenger.media.exo;

import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * Created by r.kolbasa on 28.11.2017.
 * Phoenix-for-VK
 */
public class ExoUtil {
    public static void pausePlayer(SimpleExoPlayer player) {
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    public static void startPlayer(SimpleExoPlayer player) {
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
}