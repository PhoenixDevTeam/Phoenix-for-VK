package biz.dealnote.messenger.media.gif;

import android.support.annotation.NonNull;
import android.view.SurfaceHolder;

import biz.dealnote.messenger.model.VideoSize;

/**
 * Created by Ruslan Kolbasa on 13.07.2017.
 * phoenix
 */
public interface IGifPlayer {

    VideoSize getVideoSize();

    void play() throws PlayerPrepareException;

    void pause();

    void setDisplay(SurfaceHolder holder);

    void release();

    void addVideoSizeChangeListener(IVideoSizeChangeListener listener);

    void addStatusChangeListener(IStatusChangeListener listener);

    void removeVideoSizeChangeListener(IVideoSizeChangeListener listener);

    void removeStatusChangeListener(IStatusChangeListener listener);

    int getPlayerStatus();

    interface IStatus {
        int INIT = 1;
        int PREPARING = 2;
        int PREPARED = 3;
    }

    interface IVideoSizeChangeListener {
        void onVideoSizeChanged(@NonNull IGifPlayer player, VideoSize size);
    }

    interface IStatusChangeListener {
        void onPlayerStatusChange(@NonNull IGifPlayer player, int previousStatus, int currentStatus);
    }
}