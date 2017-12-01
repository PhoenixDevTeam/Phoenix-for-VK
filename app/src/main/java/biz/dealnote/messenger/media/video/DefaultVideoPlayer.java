package biz.dealnote.messenger.media.video;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.model.VideoSize;

/**
 * Created by Ruslan Kolbasa on 14.08.2017.
 * phoenix
 */
public class DefaultVideoPlayer implements IVideoPlayer {

    private final MediaPlayer player;

    private boolean supposedToBePlaying;

    public DefaultVideoPlayer(String fileUrl) throws IOException {
        this.player = initInternalPlayer(fileUrl);
    }

    private boolean prepared;

    private static final class OnPreparedListener implements MediaPlayer.OnPreparedListener {

        final WeakReference<DefaultVideoPlayer> ref;

        private OnPreparedListener(DefaultVideoPlayer player) {
            this.ref = new WeakReference<>(player);
        }

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            DefaultVideoPlayer player = ref.get();
            if (player != null) {
                player.onPlayerPrepared();
            }
        }
    }

    private void onPlayerPrepared() {
        this.preparing = false;
        this.prepared = true;

        if (supposedToBePlaying) {
            player.start();
        }
    }

    private final OnPreparedListener preparedListener = new OnPreparedListener(this);

    private MediaPlayer initInternalPlayer(String url) throws IOException {
        MediaPlayer player = new MediaPlayer();
        player.setOnPreparedListener(preparedListener);
        player.setOnBufferingUpdateListener(bufferingUpdateListener);
        player.setOnVideoSizeChangedListener(videoSizeChangedListener);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDataSource(url);
        return player;
    }

    @Override
    public void play() {
        if (supposedToBePlaying) {
            return;
        }

        supposedToBePlaying = true;

        if (prepared) {
            player.start();
        } else if (!preparing) {
            preparing = true;
            player.prepareAsync();
        }
    }

    private boolean preparing;

    @Override
    public void pause() {
        if (!supposedToBePlaying) {
            return;
        }

        supposedToBePlaying = false;

        if (prepared) {
            player.pause();
        }
    }

    private boolean released;

    @Override
    public void release() {
        try {
            player.setOnBufferingUpdateListener(null);
            player.setOnVideoSizeChangedListener(null);
            player.setOnPreparedListener(null);
            player.release();

            released = true;
        } catch (Exception ignored) {

        }
    }

    @Override
    public int getDuration() {
        try {
            return player.getDuration();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        try {
            return player.getCurrentPosition();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void seekTo(int position) {
        try {
            player.seekTo(position);
        } catch (Exception ignored) {

        }
    }

    @Override
    public boolean isPlaying() {
        return supposedToBePlaying;
    }

    private static final class OnBufferingUpdateListener implements MediaPlayer.OnBufferingUpdateListener {

        final WeakReference<DefaultVideoPlayer> ref;

        private OnBufferingUpdateListener(DefaultVideoPlayer player) {
            this.ref = new WeakReference<>(player);
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
            DefaultVideoPlayer player = ref.get();
            if (player != null) {
                player.onBufferingUpdate(i);
            }
        }
    }

    private final OnBufferingUpdateListener bufferingUpdateListener = new OnBufferingUpdateListener(this);

    private int buffer;

    private final OnVideoSizeChangedListener videoSizeChangedListener = new OnVideoSizeChangedListener(this);

    private static final class OnVideoSizeChangedListener implements MediaPlayer.OnVideoSizeChangedListener {

        final WeakReference<DefaultVideoPlayer> ref;

        private OnVideoSizeChangedListener(DefaultVideoPlayer player) {
            this.ref = new WeakReference<>(player);
        }

        @Override
        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
            DefaultVideoPlayer player = ref.get();
            if (player != null) {
                player.onVideoSizeChanged(i, i1);
            }
        }
    }

    private void onVideoSizeChanged(int w, int h) {
        for (IVideoSizeChangeListener listener : videoSizeChangeListeners) {
            listener.onVideoSizeChanged(this, new VideoSize(w, h));
        }
    }

    private void onBufferingUpdate(int buffer) {
        this.buffer = buffer;
    }

    @Override
    public int getBufferPercentage() {
        return buffer;
    }

    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {
        player.setDisplay(holder);
    }

    private final List<IVideoSizeChangeListener> videoSizeChangeListeners = new ArrayList<>(1);

    @Override
    public void addVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.add(listener);
    }

    @Override
    public void removeVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.remove(listener);
    }
}