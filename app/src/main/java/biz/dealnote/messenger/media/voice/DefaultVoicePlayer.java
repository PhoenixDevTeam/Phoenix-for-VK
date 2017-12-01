package biz.dealnote.messenger.media.voice;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;

import java.io.IOException;

import biz.dealnote.messenger.model.VoiceMessage;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Optional;

/**
 * Created by admin on 09.10.2016.
 * phoenix
 */
public class DefaultVoicePlayer implements IVoicePlayer, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private int mStatus;
    private MediaPlayer mPlayer;
    private IPlayerStatusListener mCallback;
    private IErrorListener mErrorListener;
    private int mDuration;

    public boolean toggle(int id, VoiceMessage audio) throws PrepareException {
        if (Objects.nonNull(mPlayingEntry) && mPlayingEntry.getId() == id) {
            setSupposedToPlay(!isSupposedToPlay());
            return false;
        }

        stop();

        mPlayingEntry = new AudioEntry(id, audio);
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
        changeStatusTo(STATUS_PREPARING);

        mPlayer.prepareAsync();
        return true;
    }

    public float getProgress() {
        if (Objects.isNull(mPlayer) || mStatus != STATUS_PREPARED) {
            return 0f;
        }
        return (float) mPlayer.getCurrentPosition() / (float) mDuration;
    }

    @Override
    public void setCallback(@Nullable IPlayerStatusListener listener) {
        this.mCallback = listener;
    }

    @Override
    public void setErrorListener(@Nullable IErrorListener errorListener) {
        this.mErrorListener = errorListener;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mp != mPlayer) {
            return;
        }
        changeStatusTo(STATUS_PREPARED);

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

        changeStatusTo(STATUS_NO_PLAYBACK);
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
        if(Objects.nonNull(mErrorListener) && mPlayer == mp){
            mErrorListener.onPlayError(new Exception("Unable to play message, what: " + what + ", extra: " + extra));
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp != mPlayer) return;

        setSupposedToPlay(false);
        changeStatusTo(STATUS_PREPARED);
    }

    public boolean isSupposedToPlay() {
        return mSupposedToPlay;
    }

    private void setSupposedToPlay(boolean supposedToPlay) {
        if (supposedToPlay == mSupposedToPlay) {
            return;
        }

        this.mSupposedToPlay = supposedToPlay;

        if (mStatus == STATUS_PREPARED) {
            if (supposedToPlay) {
                mPlayer.start();
            } else {
                mPlayer.pause();
            }
        }
    }

    private AudioEntry mPlayingEntry;

    public Optional<Integer> getPlayingVoiceId() {
        return mPlayingEntry == null ? Optional.empty() : Optional.wrap(mPlayingEntry.getId());
    }

    private boolean mSupposedToPlay;
}