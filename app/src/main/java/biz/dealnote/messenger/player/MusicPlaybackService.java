/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package biz.dealnote.messenger.player;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import biz.dealnote.messenger.BuildConfig;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IAudioInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.isEmpty;

public class MusicPlaybackService extends Service {

    private static final String TAG = "MusicPlaybackService";
    private static final boolean D = BuildConfig.DEBUG;

    public static final String PLAYSTATE_CHANGED = "com.apollo.playstatechanged";
    public static final String POSITION_CHANGED = "com.apollo.positionchanged";
    public static final String META_CHANGED = "com.apollo.metachanged";
    public static final String PREPARED = "com.apollo.prepared";
    public static final String REPEATMODE_CHANGED = "com.apollo.repeatmodechanged";
    public static final String SHUFFLEMODE_CHANGED = "com.apollo.shufflemodechanged";
    public static final String QUEUE_CHANGED = "com.apollo.queuechanged";


    /**
     * Called to indicate a general service commmand. Used in
     * {@link MediaButtonIntentReceiver}
     */
    public static final String SERVICECMD = "com.apollo.musicservicecommand";
    public static final String TOGGLEPAUSE_ACTION = "com.apollo.togglepause";
    public static final String PAUSE_ACTION = "com.apollo.pause";
    public static final String STOP_ACTION = "com.apollo.stop";
    public static final String PREVIOUS_ACTION = "com.apollo.previous";
    public static final String NEXT_ACTION = "com.apollo.next";
    public static final String REPEAT_ACTION = "com.apollo.repeat";
    public static final String SHUFFLE_ACTION = "com.apollo.shuffle";

    /**
     * Called to update the service about the foreground state of Apollo's activities
     */
    public static final String FOREGROUND_STATE_CHANGED = "com.apollo.fgstatechanged";
    public static final String NOW_IN_FOREGROUND = "nowinforeground";
    public static final String FROM_MEDIA_BUTTON = "frommediabutton";
    public static final String REFRESH = "com.apollo.refresh";
    final String SHUTDOWN = "com.apollo.shutdown";

    /**
     * Called to update the remote control client
     */
    public static final String UPDATE_LOCKSCREEN = "com.apollo.updatelockscreen";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String CMDPLAYLIST = "playlist";


    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE = 1;

    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;

    private static final int TRACK_ENDED = 1;
    private static final int TRACK_WENT_TO_NEXT = 2;
    private static final int RELEASE_WAKELOCK = 3;
    private static final int SERVER_DIED = 4;
    private static final int FOCUSCHANGE = 5;
    private static final int FADEDOWN = 6;
    private static final int FADEUP = 7;

    private static final int IDLE_DELAY = 60000;
    private static final int MAX_HISTORY_SIZE = 100;
    private static final LinkedList<Integer> mHistory = new LinkedList<>();

    private static final Shuffler mShuffler = new Shuffler(MAX_HISTORY_SIZE);

    private final IBinder mBinder = new ServiceStub(this);
    private MultiPlayer mPlayer;
    private WakeLock mWakeLock;

    private AlarmManager mAlarmManager;
    private PendingIntent mShutdownIntent;
    private boolean mShutdownScheduled;

    private AudioManager mAudioManager;

    private boolean mIsSupposedToBePlaying = false;

    /**
     * Used to track what type of audio focus loss caused the playback to pause
     */
    private boolean mPausedByTransientLossOfFocus = false;

    private boolean mAnyActivityInForeground = false;

    private MediaSessionCompat mMediaSession;

    private MediaControllerCompat.TransportControls mTransportController;

    private ComponentName mMediaButtonReceiverComponent;

    private int mPlayPos = -1;

    private int mShuffleMode = SHUFFLE_NONE;

    private int mRepeatMode = REPEAT_NONE;

    private List<Audio> mPlayList = null;

    private MusicPlayerHandler mPlayerHandler;

    private NotificationHelper mNotificationHelper;

    @Override
    public IBinder onBind(final Intent intent) {
        if (D) Logger.d(TAG, "Service bound, intent = " + intent);
        cancelShutdown();
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        if (D) Logger.d(TAG, "Service unbound");

        if (mIsSupposedToBePlaying || mPausedByTransientLossOfFocus || isPreparing()) {
            Logger.d(TAG, "onUnbind, mIsSupposedToBePlaying || mPausedByTransientLossOfFocus || isPreparing()");
            return true;


        } else if (Utils.safeIsEmpty(mPlayList) || mPlayerHandler.hasMessages(TRACK_ENDED)) {
            scheduleDelayedShutdown();
            Logger.d(TAG, "onUnbind, scheduleDelayedShutdown");
            return true;
        }

        stopSelf();
        Logger.d(TAG, "onUnbind, stopSelf(mServiceStartId)");
        return true;
    }

    @Override
    public void onRebind(final Intent intent) {
        cancelShutdown();
    }

    @Override
    public void onCreate() {
        if (D) Logger.d(TAG, "Creating service");
        super.onCreate();

        mNotificationHelper = new NotificationHelper(this);

        final HandlerThread thread = new HandlerThread("MusicPlayerHandler", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mPlayerHandler = new MusicPlayerHandler(this, thread.getLooper());

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());

        setUpRemoteControlClient();

        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICECMD);
        filter.addAction(TOGGLEPAUSE_ACTION);
        filter.addAction(PAUSE_ACTION);
        filter.addAction(STOP_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);
        filter.addAction(REPEAT_ACTION);
        filter.addAction(SHUFFLE_ACTION);

        registerReceiver(mIntentReceiver, filter);

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);

        // Initialize the delayed shutdown intent
        final Intent shutdownIntent = new Intent(this, MusicPlaybackService.class);
        shutdownIntent.setAction(SHUTDOWN);

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mShutdownIntent = PendingIntent.getService(this, 0, shutdownIntent, 0);

        // Listen for the idle state
        scheduleDelayedShutdown();

        notifyChange(META_CHANGED);
    }

    private void setUpRemoteControlClient() {
        final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mMediaButtonReceiverComponent);

        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mMediaSession = new MediaSessionCompat(getApplication(), "TAG", mMediaButtonReceiverComponent, null);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_SEEK_TO |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_STOP
                )
                .setState(isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, position(), 1.0f)
                .build();

        mMediaSession.setPlaybackState(playbackStateCompat);
        mMediaSession.setCallback(mMediaSessionCallback);
        mMediaSession.setActive(true);
        updateRemoteControlClient(META_CHANGED);
        mTransportController = mMediaSession.getController().getTransportControls();

    }

    private final MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            play();
        }

        @Override
        public void onPause() {
            super.onPause();
            pause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            gotoNext(true);
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            prev();
        }

        @Override
        public void onStop() {
            super.onStop();
            pause();
            Logger.d(getClass().getSimpleName(), "Stopping services. onStop()");
            stopSelf();
        }
    };

    @Override
    public void onDestroy() {
        if (D) Logger.d(TAG, "Destroying service");
        super.onDestroy();

        final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);

        mAlarmManager.cancel(mShutdownIntent);

        mPlayerHandler.removeCallbacksAndMessages(null);

        mPlayer.release();
        mPlayer = null;

        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        mMediaSession.release();

        mPlayerHandler.removeCallbacksAndMessages(null);

        unregisterReceiver(mIntentReceiver);

        mWakeLock.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (D) Logger.d(TAG, "Got new intent " + intent + ", startId = " + startId);

        if (intent != null) {
            final String action = intent.getAction();

            if (intent.hasExtra(NOW_IN_FOREGROUND)) {
                mAnyActivityInForeground = intent.getBooleanExtra(NOW_IN_FOREGROUND, false);
                updateNotification();
            }

            if (SHUTDOWN.equals(action)) {
                mShutdownScheduled = false;
                releaseServiceUiAndStop();
                return START_NOT_STICKY;
            }

            handleCommandIntent(intent);
        }

        // Make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
        scheduleDelayedShutdown();

        if (intent != null && intent.getBooleanExtra(FROM_MEDIA_BUTTON, false)) {
            MediaButtonIntentReceiver.completeWakefulIntent(intent);
        }

        return START_STICKY;
    }

    private void releaseServiceUiAndStop() {
        if (isPlaying() || mPausedByTransientLossOfFocus || mPlayerHandler.hasMessages(TRACK_ENDED)) {
            return;
        }

        if (D) Logger.d(TAG, "Nothing is playing anymore, releasing notification");

        mNotificationHelper.killNotification();
        mAudioManager.abandonAudioFocus(mAudioFocusListener);

        if (!mAnyActivityInForeground) {
            stopSelf();
        }
    }

    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        final String command = SERVICECMD.equals(action) ? intent.getStringExtra(CMDNAME) : null;

        if (D) Logger.d(TAG, "handleCommandIntent: action = " + action + ", command = " + command);

        if (CMDNEXT.equals(command) || NEXT_ACTION.equals(action)) {
            mTransportController.skipToNext();
        }
        if (CMDPREVIOUS.equals(command) || PREVIOUS_ACTION.equals(action)) {
            mTransportController.skipToPrevious();
        }

        if (CMDTOGGLEPAUSE.equals(command) || TOGGLEPAUSE_ACTION.equals(action)) {
            if (isPlaying()) {
                mTransportController.pause();
                mPausedByTransientLossOfFocus = false;
            } else {
                mTransportController.play();
            }
        }

        if (CMDPAUSE.equals(command) || PAUSE_ACTION.equals(action)) {
            mTransportController.pause();
            mPausedByTransientLossOfFocus = false;
        }

        if (CMDPLAY.equals(command)) {
            play();
        }
        if (CMDSTOP.equals(command) || STOP_ACTION.equals(action)) {
            mTransportController.pause();
            mPausedByTransientLossOfFocus = false;
            seek(0);
            releaseServiceUiAndStop();
        }

        if (REPEAT_ACTION.equals(action)) {
            cycleRepeat();
        }

        if (SHUFFLE_ACTION.equals(action)) {
            cycleShuffle();
        }

        if (CMDPLAYLIST.equals(action)) {
            ArrayList<Audio> apiAudios = intent.getParcelableArrayListExtra(Extra.AUDIOS);
            int position = intent.getIntExtra(Extra.POSITION, 0);
            int forceShuffle = intent.getIntExtra(Extra.SHUFFLE_MODE, SHUFFLE_NONE);
            setShuffleMode(forceShuffle);
            open(apiAudios, position);
        }
    }

    public static final int MAX_QUEUE_SIZE = 200;

    public static void startForPlayList(Context context, @NonNull ArrayList<Audio> audios, int position, boolean forceShuffle) {
        Logger.d(TAG, "startForPlayList, count: " + audios.size() + ", position: " + position);

        ArrayList<Audio> target;
        int targetPosition;

        if (audios.size() <= MAX_QUEUE_SIZE) {
            target = audios;
            targetPosition = position;
        } else {
            target = new ArrayList<>(MusicPlaybackService.MAX_QUEUE_SIZE);
            int half = MusicPlaybackService.MAX_QUEUE_SIZE / 2;

            int startAt = position - half;
            if (startAt < 0) {
                startAt = 0;
            }

            targetPosition = position - startAt;
            for (int i = startAt; target.size() < MusicPlaybackService.MAX_QUEUE_SIZE; i++) {
                if (i > audios.size() - 1) {
                    break;
                }

                target.add(audios.get(i));
            }

            if (target.size() < MusicPlaybackService.MAX_QUEUE_SIZE) {
                for (int i = startAt - 1; target.size() < MusicPlaybackService.MAX_QUEUE_SIZE; i--) {
                    target.add(0, audios.get(i));
                    targetPosition++;
                }
            }
        }

        Intent intent = new Intent(context, MusicPlaybackService.class);
        intent.setAction(CMDPLAYLIST);
        intent.putParcelableArrayListExtra(Extra.AUDIOS, target);
        intent.putExtra(Extra.POSITION, targetPosition);
        intent.putExtra(Extra.SHUFFLE_MODE, forceShuffle ? SHUFFLE : SHUFFLE_NONE);
        context.startService(intent);
    }

    /**
     * Updates the notification, considering the current play and activity state
     */
    private void updateNotification() {
        mNotificationHelper.buildNotification(getApplicationContext(), getAlbumName(), getArtistName(),
                getTrackName(), null, null, isPlaying(), mMediaSession.getSessionToken());
    }

    private void scheduleDelayedShutdown() {
        if (D) Log.v(TAG, "Scheduling shutdown in " + IDLE_DELAY + " ms");
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + IDLE_DELAY, mShutdownIntent);
        mShutdownScheduled = true;
    }

    private void cancelShutdown() {
        if (D) Logger.d(TAG, "Cancelling delayed shutdown, scheduled = " + mShutdownScheduled);
        if (mShutdownScheduled) {
            mAlarmManager.cancel(mShutdownIntent);
            mShutdownScheduled = false;
        }
    }

    /**
     * Stops playback
     *
     * @param goToIdle True to go to the idle state, false otherwise
     */
    private void stop(final boolean goToIdle) {
        if (D) Logger.d(TAG, "Stopping playback, goToIdle = " + goToIdle);

        if (mPlayer != null && mPlayer.isInitialized()) {
            mPlayer.stop();
        }

        if (goToIdle) {
            scheduleDelayedShutdown();
            mIsSupposedToBePlaying = false;
        } else {
            stopForeground(false); //надо подумать
        }
    }

    private boolean isInitialized() {
        return mPlayer != null && mPlayer.isInitialized();
    }

    private boolean isPreparing() {
        return mPlayer != null && mPlayer.isPreparing();
    }

    /**
     * Called to open a new file as the current track and prepare the next for
     * playback
     */
    private void playCurrentTrack() {
        synchronized (this) {
            Logger.d(TAG, "playCurrentTrack, mPlayListLen: " + Utils.safeCountOf(mPlayList));

            if (Utils.safeIsEmpty(mPlayList)) {
                return;
            }

            stop(Boolean.FALSE);

            Audio current = mPlayList.get(mPlayPos);
            openFile(current);
        }
    }

    /**
     * @param force True to force the player onto the track next, false
     *              otherwise.
     * @return The next position to play.
     */
    private int getNextPosition(final boolean force) {
        if (!force && mRepeatMode == REPEAT_CURRENT) {
            return mPlayPos < 0 ? 0 : mPlayPos;
        }

        if (mShuffleMode == SHUFFLE) {
            if (mPlayPos >= 0) {
                mHistory.add(mPlayPos);
            }

            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }

            Stack<Integer> notPlayedTracksPositions = new Stack<>();
            boolean allWerePlayed = mPlayList.size() - mHistory.size() == 0;
            if (!allWerePlayed) {
                for (int i = 0; i < mPlayList.size(); i++) {
                    if (!mHistory.contains(i)) {
                        notPlayedTracksPositions.push(i);
                    }
                }
            } else {
                for (int i = 0; i < mPlayList.size(); i++) {
                    notPlayedTracksPositions.push(i);
                }
                mHistory.clear();
            }
            return notPlayedTracksPositions.get(mShuffler.nextInt(notPlayedTracksPositions.size()));
        }

        if (mPlayPos >= Utils.safeCountOf(mPlayList) - 1) {
            if (mRepeatMode == REPEAT_NONE && !force) {
                return -1;
            }
            if (mRepeatMode == REPEAT_ALL || force) {
                return 0;
            }
            return -1;
        } else {
            return mPlayPos + 1;
        }
    }

    /**
     * Notify the change-receivers that something has changed.
     */
    private void notifyChange(final String what) {
        if (D) Logger.d(TAG, "notifyChange: what = " + what);

        updateRemoteControlClient(what);

        if (what.equals(POSITION_CHANGED)) {
            return;
        }

        final Intent intent = new Intent(what);
        intent.putExtra("id", getCurrentTrack());
        intent.putExtra("artist", getArtistName());
        intent.putExtra("album", getAlbumName());
        intent.putExtra("track", getTrackName());
        intent.putExtra("playing", isPlaying());
        sendStickyBroadcast(intent);

        if (what.equals(PLAYSTATE_CHANGED)) {
            mNotificationHelper.updatePlayState(isPlaying());
        }
    }

    /**
     * Updates the lockscreen controls.
     *
     * @param what The broadcast
     */
    private void updateRemoteControlClient(final String what) {
        int playState = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        PlaybackStateCompat pmc = new PlaybackStateCompat.Builder()
                .setState(playState, position(), 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build();
        switch (what) {
            case PLAYSTATE_CHANGED:
            case POSITION_CHANGED:
                mMediaSession.setPlaybackState(pmc);
                break;
            case META_CHANGED:
                mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getArtistName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getAlbumName())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getTrackName())
                        //.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getAlbumCover())
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration())
                        .build());

                break;
        }
    }

    /**
     * Opens a file and prepares it for playback
     *
     * @param audio The path of the file to open
     */
    public void openFile(final Audio audio) {
        synchronized (this) {
            if (audio == null) {
                stop(Boolean.TRUE);
                return;
            }

            mPlayer.setDataSource(audio.getOwnerId(), audio.getId(), audio.getUrl());
        }
    }

    /**
     * Returns the audio session ID
     *
     * @return The current media player audio session ID
     */
    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    /**
     * Returns the audio session ID
     *
     * @return The current media player audio session ID
     */
    public int getBufferPercent() {
        synchronized (this) {
            return mPlayer.getBufferPercent();
        }
    }

    public int getShuffleMode() {
        return mShuffleMode;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    public String getPath() {
        synchronized (this) {
            Audio apiAudio = getCurrentTrack();
            if (apiAudio == null) {
                return null;
            }

            return apiAudio.getUrl();
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            if (getCurrentTrack() == null) {
                return null;
            }

            return String.valueOf(getCurrentTrack().getAlbumId());
        }
    }

    /**
     * Returns the album cover
     *
     * @return Bitmap
     */
    public Bitmap getAlbumCover() {
        synchronized (this) {
            if (getCurrentTrack() == null) {
                return null;
            }

            Bitmap cover = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
            return cover;
        }
    }

    public String getTrackName() {
        synchronized (this) {
            Audio current = getCurrentTrack();
            if (current == null) {
                return null;
            }

            return current.getTitle();
        }
    }

    public String getArtistName() {
        synchronized (this) {
            Audio current = getCurrentTrack();
            if (current == null) {
                return null;
            }

            return current.getArtist();
        }
    }

    public Audio getCurrentTrack() {
        synchronized (this) {
            if (mPlayPos >= 0) {
                return mPlayList.get(mPlayPos);
            }
        }

        return null;
    }

    public long seek(long position) {
        if (mPlayer != null && mPlayer.isInitialized()) {
            if (position < 0) {
                position = 0;
            } else if (position > mPlayer.duration()) {
                position = mPlayer.duration();
            }

            long result = mPlayer.seek(position);
            notifyChange(POSITION_CHANGED);
            return result;
        }

        return -1;
    }

    public long position() {
        if (mPlayer != null && mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    public long duration() {
        if (mPlayer != null && mPlayer.isInitialized()) {
            return mPlayer.duration();
        }

        return -1;
    }

    public List<Audio> getQueue() {
        synchronized (this) {
            final int len = Utils.safeCountOf(mPlayList);
            final List<Audio> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(i, mPlayList.get(i));
            }

            return list;
        }
    }

    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    /**
     * Opens a list for playback
     *
     * @param list     The list of tracks to open
     * @param position The position to start playback at
     */
    public void open(@NonNull final List<Audio> list, final int position) {
        synchronized (this) {
            final Audio oldAudio = getCurrentTrack();

            mPlayList = list;

            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mShuffler.nextInt(Utils.safeCountOf(mPlayList));
            }

            Logger.d(TAG, "open, size: " + mPlayList.size() + ", position: " + mPlayPos);

            mHistory.clear();

            playCurrentTrack();

            notifyChange(QUEUE_CHANGED);
            if (oldAudio != getCurrentTrack()) {
                notifyChange(META_CHANGED);
            }
        }
    }


    public void stop() {
        stop(true);
    }


    public void play() {
        int status = mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (D) {
            Logger.d(TAG, "Starting playback: audio focus request status = " + status);
        }

        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }

        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName()));
        if (mPlayer != null && mPlayer.isInitialized()) {

            final long duration = mPlayer.duration();
            if (mRepeatMode != REPEAT_CURRENT && duration > 2000 && mPlayer.position() >= duration - 2000) {
                gotoNext(false);
            }

            mPlayer.start();
            mPlayerHandler.removeMessages(FADEDOWN);
            mPlayerHandler.sendEmptyMessage(FADEUP);

            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyChange(PLAYSTATE_CHANGED);
            }

            cancelShutdown();
            updateNotification();
        }
    }

    /**
     * Temporarily pauses playback.
     */
    public void pause() {
        if (D) Logger.d(TAG, "Pausing playback");
        synchronized (this) {
            mPlayerHandler.removeMessages(FADEUP);
            if (mPlayer != null && mIsSupposedToBePlaying) {
                mPlayer.pause();
                scheduleDelayedShutdown();
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
            }
        }
    }

    /**
     * Changes from the current track to the next track
     */
    public void gotoNext(final boolean force) {
        if (D) Logger.d(TAG, "Going to next track");

        synchronized (this) {
            if (Utils.safeCountOf(mPlayList) <= 0) {
                if (D) Logger.d(TAG, "No play queue");

                scheduleDelayedShutdown();
                return;
            }

            final int pos = getNextPosition(force);
            Logger.d(TAG, String.valueOf(pos));

            if (pos < 0) {
                pause();
                scheduleDelayedShutdown();
                if (mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                }

                return;
            }

            mPlayPos = pos;
            stop(false);
            mPlayPos = pos;

            playCurrentTrack();

            notifyChange(META_CHANGED);
        }
    }

    /**
     * Changes from the current track to the previous played track
     */
    public void prev() {
        if (D) Logger.d(TAG, "Going to previous track");

        synchronized (this) {
            if (mShuffleMode == SHUFFLE) {
                // Go to previously-played track and remove it from the history
                final int histsize = mHistory.size();
                if (histsize == 0) {
                    return;
                }

                mPlayPos = mHistory.remove(histsize - 1);
            } else {
                if (mPlayPos > 0) {
                    mPlayPos--;
                } else {
                    mPlayPos = Utils.safeCountOf(mPlayList) - 1;
                }
            }

            stop(false);
            playCurrentTrack();

            notifyChange(META_CHANGED);
        }
    }

    public void setRepeatMode(final int repeatmode) {
        synchronized (this) {
            mRepeatMode = repeatmode;
            notifyChange(REPEATMODE_CHANGED);
        }
    }

    public void setShuffleMode(final int shufflemode) {
        synchronized (this) {
            if (mShuffleMode == shufflemode && Utils.safeCountOf(mPlayList) > 0) {
                return;
            }

            mShuffleMode = shufflemode;
            notifyChange(SHUFFLEMODE_CHANGED);
        }
    }

    private void cycleRepeat() {
        switch (mRepeatMode) {
            case REPEAT_NONE:
                setRepeatMode(REPEAT_ALL);
                break;
            case REPEAT_ALL:
                setRepeatMode(REPEAT_CURRENT);
                if (mShuffleMode != SHUFFLE_NONE) {
                    setShuffleMode(SHUFFLE_NONE);
                }
                break;
            default:
                setRepeatMode(REPEAT_NONE);
                break;
        }
    }

    private void cycleShuffle() {
        switch (mShuffleMode) {
            case SHUFFLE:
                setShuffleMode(SHUFFLE_NONE);
                break;
            case SHUFFLE_NONE:
                setShuffleMode(SHUFFLE);
                if (mRepeatMode == REPEAT_CURRENT) {
                    setRepeatMode(REPEAT_ALL);
                }
                break;
        }
    }

    /**
     * Called when one of the lists should refresh or requery.
     */
    public void refresh() {
        notifyChange(REFRESH);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            handleCommandIntent(intent);
        }
    };

    private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(final int focusChange) {
            mPlayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };

    private static final class MusicPlayerHandler extends Handler {

        private final WeakReference<MusicPlaybackService> mService;
        private float mCurrentVolume = 1.0f;

        /**
         * Constructor of <code>MusicPlayerHandler</code>
         *
         * @param service The service to use.
         * @param looper  The thread to run on.
         */
        public MusicPlayerHandler(final MusicPlaybackService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(final Message msg) {
            final MusicPlaybackService service = mService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= .05f;
                    if (mCurrentVolume > .2f) {
                        sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    mCurrentVolume += .01f;
                    if (mCurrentVolume < 1.0f) {
                        sendEmptyMessageDelayed(FADEUP, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }

                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    if (service.isPlaying()) {
                        service.gotoNext(true);
                    } else {
                        service.playCurrentTrack();
                    }
                    break;
                case TRACK_WENT_TO_NEXT:
                    //service.mPlayPos = service.mNextPlayPos;
                    service.notifyChange(META_CHANGED);
                    service.updateNotification();
                    break;
                case TRACK_ENDED:
                    if (service.mRepeatMode == REPEAT_CURRENT) {
                        service.seek(0);
                        service.play();
                    } else {
                        service.gotoNext(false);
                    }
                    break;
                case RELEASE_WAKELOCK:
                    service.mWakeLock.release();
                    break;
                case FOCUSCHANGE:
                    if (D) Logger.d(TAG, "Received audio focus change event " + msg.arg1);
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            if (service.isPlaying()) {
                                service.mPausedByTransientLossOfFocus =
                                        msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                            }
                            service.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            removeMessages(FADEUP);
                            sendEmptyMessage(FADEDOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (!service.isPlaying() && service.mPausedByTransientLossOfFocus) {
                                service.mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                service.mPlayer.setVolume(mCurrentVolume);
                                service.play();
                            } else {
                                removeMessages(FADEDOWN);
                                sendEmptyMessage(FADEUP);
                            }
                            break;
                        default:
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static final class MultiPlayer implements MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

        final WeakReference<MusicPlaybackService> mService;

        MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

        Handler mHandler;

        boolean mIsInitialized;

        boolean preparing;

        int bufferPercent;

        final IAudioInteractor audioInteractor;

        final CompositeDisposable compositeDisposable = new CompositeDisposable();

        /**
         * Constructor of <code>MultiPlayer</code>
         */
        MultiPlayer(final MusicPlaybackService service) {
            mService = new WeakReference<>(service);
            audioInteractor = InteractorFactory.createAudioInteractor();
            mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
        }

        /**
         * @param path The path of the file, or the http/rtsp URL of the stream
         *             you want to play
         *             return True if the <code>player</code> has been prepared and is
         *             ready to play, false otherwise
         */
        void setDataSource(final String path) {
            Logger.d(TAG, "setDataSourceImpl, path: " + path);

            try {
                mCurrentMediaPlayer.reset();
                mCurrentMediaPlayer.setOnPreparedListener(this);
                mCurrentMediaPlayer.setDataSource(path);
                mCurrentMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mCurrentMediaPlayer.prepareAsync();
                preparing = true;

                mService.get().mIsSupposedToBePlaying = false;
            } catch (final IOException e) {
                e.printStackTrace();
            }

            mCurrentMediaPlayer.setOnCompletionListener(this);
            mCurrentMediaPlayer.setOnErrorListener(this);
            mCurrentMediaPlayer.setOnBufferingUpdateListener(this);

            final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mService.get().getPackageName());
            mService.get().sendBroadcast(intent);

            //return true;

            resetBufferPercent();
            mService.get().notifyChange(PLAYSTATE_CHANGED);
        }

        void setDataSource(int ownerId, int audioId, String url) {
            if (isEmpty(url) || "https://vk.com/mp3/audio_api_unavailable.mp3".equals(url)) {
                compositeDisposable.add(audioInteractor.findAudioUrl(audioId, ownerId)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(this::setDataSource, ignored -> setDataSource(url)));
            } else {
                setDataSource(url);
            }
        }

        void resetBufferPercent() {
            bufferPercent = 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCompletion(final MediaPlayer mp) {
            mService.get().gotoNext(false);
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            boolean current = mp == mCurrentMediaPlayer;

            Logger.d(TAG, "onPrepared, current: " + current);

            if (current) {
                preparing = false;
                mIsInitialized = true;
                mService.get().notifyChange(PREPARED);

                mService.get().play();
            }
        }

        /**
         * Sets the handler
         *
         * @param handler The handler to use
         */
        public void setHandler(final Handler handler) {
            mHandler = handler;
        }

        boolean isInitialized() {
            return mIsInitialized;
        }

        boolean isPreparing() {
            return preparing;
        }

        public void start() {
            mCurrentMediaPlayer.start();
        }

        public void stop() {
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
            preparing = false;
            resetBufferPercent();
        }

        public void release() {
            stop();
            mCurrentMediaPlayer.release();
            compositeDisposable.dispose();
        }

        public void pause() {
            mCurrentMediaPlayer.pause();
        }

        public long duration() {
            return mCurrentMediaPlayer.getDuration();
        }

        public long position() {
            return mCurrentMediaPlayer.getCurrentPosition();
        }

        long seek(final long whereto) {
            mCurrentMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        void setVolume(final float vol) {
            try {
                mCurrentMediaPlayer.setVolume(vol, vol);
            } catch (IllegalStateException ignored) {
                // случается
            }
        }

        int getAudioSessionId() {
            return mCurrentMediaPlayer.getAudioSessionId();
        }

        @Override
        public boolean onError(final MediaPlayer mp, final int what, final int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInitialized = false;
                    mCurrentMediaPlayer.release();
                    mCurrentMediaPlayer = new MediaPlayer();
                    mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                    return true;
                default:
                    break;
            }

            return false;
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mCurrentMediaPlayer == mp) {
                bufferPercent = percent;
            }
        }

        int getBufferPercent() {
            return bufferPercent;
        }
    }

    private static final class ServiceStub extends IAudioPlayerService.Stub {

        private final WeakReference<MusicPlaybackService> mService;

        private ServiceStub(final MusicPlaybackService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void openFile(final Audio audio) throws RemoteException {
            mService.get().openFile(audio);
        }

        @Override
        public void open(final List<Audio> list, final int position) throws RemoteException {
            mService.get().open(list, position);
        }

        @Override
        public void stop() throws RemoteException {
            mService.get().stop();
        }

        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        @Override
        public void play() throws RemoteException {
            mService.get().play();
        }

        @Override
        public void prev() throws RemoteException {
            mService.get().prev();
        }

        @Override
        public void next() throws RemoteException {
            mService.get().gotoNext(true);
        }

        @Override
        public void setShuffleMode(final int shufflemode) throws RemoteException {
            mService.get().setShuffleMode(shufflemode);
        }

        @Override
        public void setRepeatMode(final int repeatmode) throws RemoteException {
            mService.get().setRepeatMode(repeatmode);
        }

        @Override
        public void refresh() throws RemoteException {
            mService.get().refresh();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override
        public boolean isPreparing() throws RemoteException {
            return mService.get().isPreparing();
        }

        @Override
        public boolean isInitialized() throws RemoteException {
            return mService.get().isInitialized();
        }

        @Override
        public List<Audio> getQueue() throws RemoteException {
            return mService.get().getQueue();
        }

        @Override
        public long duration() throws RemoteException {
            return mService.get().duration();
        }

        @Override
        public long position() throws RemoteException {
            return mService.get().position();
        }

        @Override
        public long seek(final long position) throws RemoteException {
            return mService.get().seek(position);
        }

        @Override
        public Audio getCurrentAudio() throws RemoteException {
            return mService.get().getCurrentTrack();
        }

        @Override
        public String getArtistName() throws RemoteException {
            return mService.get().getArtistName();
        }

        @Override
        public String getTrackName() throws RemoteException {
            return mService.get().getTrackName();
        }

        @Override
        public String getAlbumName() throws RemoteException {
            return mService.get().getAlbumName();
        }

        @Override
        public String getPath() throws RemoteException {
            return mService.get().getPath();
        }

        @Override
        public int getQueuePosition() throws RemoteException {
            return mService.get().getQueuePosition();
        }

        @Override
        public int getShuffleMode() throws RemoteException {
            return mService.get().getShuffleMode();
        }

        @Override
        public int getRepeatMode() throws RemoteException {
            return mService.get().getRepeatMode();
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mService.get().getAudioSessionId();
        }

        @Override
        public int getBufferPercent() throws RemoteException {
            return mService.get().getBufferPercent();
        }
    }
}
