package biz.dealnote.messenger.media.gif;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.model.VideoSize;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public class DefaultGifPlayer implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, IGifPlayer {

    private final List<IGifPlayer.IStatusChangeListener> statusChangeListeners;
    private final List<IGifPlayer.IVideoSizeChangeListener> videoSizeChangeListeners;

    private MediaPlayer mediaPlayer;
    private boolean supposedToPlay;
    private String fileUrl;
    private int status;
    private boolean released;
    private VideoSize videoSize;

    public DefaultGifPlayer(@NonNull String url) {
        fileUrl = url;
        status = IGifPlayer.IStatus.INIT;
        statusChangeListeners = new ArrayList<>(1);
        videoSizeChangeListeners = new ArrayList<>(1);
    }

    public VideoSize getVideoSize() {
        return videoSize;
    }

    @Override
    public void play() throws PlayerPrepareException {
        if (supposedToPlay) {
            return;
        }

        supposedToPlay = true;

        switch (status) {
            case IGifPlayer.IStatus.PREPARED:
                mediaPlayer.start();
                break;
            case IGifPlayer.IStatus.INIT:
                preparePlayer();
                break;
            case IGifPlayer.IStatus.PREPARING:
                //do nothing
                break;
        }
    }

    @Override
    public void pause() {
        if (!supposedToPlay) {
            return;
        }

        supposedToPlay = false;
        mediaPlayer.pause();
    }

    private void changeStatusTo(int status) {
        if (status == this.status) {
            return;
        }
        int old = this.status;

        this.status = status;

        fireStatusChange(old, status);
    }

    private void fireStatusChange(int old, int current) {
        for (IStatusChangeListener listener : statusChangeListeners) {
            listener.onPlayerStatusChange(this, old, current);
        }
    }

    private void preparePlayer() throws PlayerPrepareException {
        try {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(fileUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setLooping(true);

            changeStatusTo(IGifPlayer.IStatus.PREPARING);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            changeStatusTo(IGifPlayer.IStatus.INIT);
            throw new PlayerPrepareException();
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if (Objects.isNull(mediaPlayer)) {
            return;
        }

        mediaPlayer.setDisplay(holder);

        if (holder != null) {
            mediaPlayer.setScreenOnWhilePlaying(true);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (isReleased()) {
            return;
        }

        changeStatusTo(IGifPlayer.IStatus.PREPARED);

        if (supposedToPlay) {
            mediaPlayer.start();
        }
    }

    private void fireVideroSizeChanged() {
        for (IVideoSizeChangeListener listener : videoSizeChangeListeners) {
            listener.onVideoSizeChanged(this, videoSize);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        videoSize = new VideoSize(width, height);
        fireVideroSizeChanged();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    private boolean isReleased() {
        return released;
    }

    public void release() {
        if (isReleased()) return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        released = true;
        changeStatusTo(IGifPlayer.IStatus.INIT);

        statusChangeListeners.clear();
        videoSizeChangeListeners.clear();
    }

    @Override
    public void addVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        videoSizeChangeListeners.add(listener);
    }

    @Override
    public void addStatusChangeListener(IStatusChangeListener listener) {
        statusChangeListeners.add(listener);
    }

    @Override
    public void removeVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        videoSizeChangeListeners.remove(listener);
    }

    @Override
    public void removeStatusChangeListener(IStatusChangeListener listener) {
        statusChangeListeners.remove(listener);
    }

    @Override
    public int getPlayerStatus() {
        return status;
    }
}