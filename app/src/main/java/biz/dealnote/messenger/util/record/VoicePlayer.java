package biz.dealnote.messenger.util.record;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.model.VoiceMessage;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by admin on 09.10.2016.
 * phoenix
 */
public class VoicePlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private int mStatus;
    private MediaPlayer mPlayer;
    private Callback mCallback;
    private int mDuration;

    public boolean toggle(int id, VoiceMessage audio) throws PrepareException {
        if (Objects.nonNull(mPlayingEntry) && mPlayingEntry.getId() == id) {
            //changeStatusTo(Status.PREPARED); // ШТО??
            setSupposedToPlay(!isSupposedToPlay());
            return false;
        }

        stop();

        mPlayingEntry = new Entry(id, audio);
        mDuration = audio.getDuration() * 1000;
        mPlayer = new MediaPlayer();

        try {
            mPlayer.setDataSource(mPlayingEntry.getAudio().getLinkMp3());
        } catch (IOException e) {
            throw new PrepareException();
        }

        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);

        mSupposedToPlay = true;
        changeStatusTo(Status.PREPARING);

        mPlayer.prepareAsync();
        return true;
    }

    public float getProgress() {
        if (Objects.isNull(mPlayer)) {
            return 0f;
        }

        if (mStatus != Status.PREPARED) {
            return 0f;
        }

        //int currentPosition = mPlayer.getCurrentPosition();
        //int duration = mPlayer.getDuration();
        //Logger.d(TAG, "currentPosition: " + currentPosition + ", duration: " + duration);
        return (float) mPlayer.getCurrentPosition() / (float) mDuration;
    }

    public void setCallback(@Nullable Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mp != mPlayer) return;
        changeStatusTo(Status.PREPARED);

        if (mSupposedToPlay) {
            mPlayer.start();
        }
    }

    public void stop() {
        if (Objects.nonNull(mPlayer)) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        changeStatusTo(Status.NO_PLAYBACK);
    }

    public void release() {
        stop();
    }

    private void changeStatusTo(int status) {
        if (mStatus == status) {
            return;
        }

        mStatus = status;

        if (Objects.nonNull(mPlayer)) {
            mCallback.onPlayerStatusChange(status);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(App.getInstance(), "Unable to play message", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp != mPlayer) return;

        mSupposedToPlay = false;
        changeStatusTo(Status.NO_PLAYBACK);
    }

    public class PrepareException extends Exception {

    }

    public boolean isSupposedToPlay() {
        return mSupposedToPlay;
    }

    private void setSupposedToPlay(boolean supposedToPlay) {
        if (supposedToPlay == mSupposedToPlay) {
            return;
        }

        this.mSupposedToPlay = supposedToPlay;

        if (mStatus == Status.PREPARED) {
            if (supposedToPlay) {
                mPlayer.start();
            } else {
                mPlayer.pause();
            }
        }
    }

    private static class Entry {

        int id;
        VoiceMessage audio;

        Entry(int id, @NonNull VoiceMessage audio) {
            this.id = id;
            this.audio = audio;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return id == entry.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        public int getId() {
            return id;
        }

        public VoiceMessage getAudio() {
            return audio;
        }
    }

    private Entry mPlayingEntry;

    public Integer getPlayingVoiceId() {
        return mPlayingEntry == null ? null : mPlayingEntry.getId();
    }

    private boolean mSupposedToPlay;

    public interface Callback {
        void onPlayerStatusChange(int status);
    }

    public static final class Status {
        public static final int NO_PLAYBACK = 0;
        public static final int PREPARING = 1;
        public static final int PREPARED = 2;
    }
}