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

package biz.dealnote.messenger.player.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.player.IAudioPlayerService;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Optional;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * A collection of helpers directly related to music or Apollo's service.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public final class MusicUtils {

    public static IAudioPlayerService mService = null;

    private static int sForegroundActivities = 0;

    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;

    static {
        mConnectionMap = new WeakHashMap<>();
    }

    /* This class is never initiated */
    private MusicUtils() {
    }

    /**
     * @param context  The {@link Context} to use
     * @param callback The {@link ServiceConnection} to use
     * @return The new instance of {@link ServiceToken}
     */
    public static ServiceToken bindToService(final Context context, final ServiceConnection callback) {
        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }

        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicPlaybackService.class));
        final ServiceBinder binder = new ServiceBinder(callback);
        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, MusicPlaybackService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }

        return null;
    }

    private static final PublishSubject<Optional<IAudioPlayerService>> SERVICE_BIND_PUBLISHER = PublishSubject.create();

    public static ServiceToken bindToServiceWithoutStart(final Activity realActivity, final ServiceConnection callback) {
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        final ServiceBinder binder = new ServiceBinder(callback);

        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, MusicPlaybackService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }

        return null;
    }

    /**
     * @param token The {@link ServiceToken} to unbind from
     */
    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }

        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }

        mContextWrapper.unbindService(mBinder);

        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static Observable<Optional<IAudioPlayerService>> observeServiceBinding(){
        return SERVICE_BIND_PUBLISHER;
    }

    public static final class ServiceBinder implements ServiceConnection {

        private final ServiceConnection mCallback;


        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mService = IAudioPlayerService.Stub.asInterface(service);

            SERVICE_BIND_PUBLISHER.onNext(Optional.wrap(mService));

            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }

            mService = null;

            SERVICE_BIND_PUBLISHER.onNext(Optional.empty());
        }
    }

    public static final class ServiceToken {

        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static String makeTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs -= hours * 3600;
        mins = secs / 60;
        secs -= mins * 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    /**
     * Changes to the next track
     */
    public static void next() {
        try {
            if (mService != null) {
                mService.next();
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static boolean isInitialized() {
        if (mService != null) {
            try {
                return mService.isInitialized();
            } catch (RemoteException ignored) {
            }
        }

        return false;
    }

    public static boolean isPreparing() {
        if (mService != null) {
            try {
                return mService.isPreparing();
            } catch (RemoteException ignored) {
            }
        }

        return false;
    }

    /**
     * Changes to the previous track.
     */
    public static void previous(final Context context) {
        final Intent previous = new Intent(context, MusicPlaybackService.class);
        previous.setAction(MusicPlaybackService.PREVIOUS_ACTION);
        context.startService(previous);
    }

    /**
     * Plays or pauses the music.
     */
    public static void playOrPause() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
            }
        } catch (final Exception ignored) {
        }
    }

    /**
     * Cycles through the repeat options.
     */
    public static void cycleRepeat() {
        try {
            if (mService != null) {
                switch (mService.getRepeatMode()) {
                    case MusicPlaybackService.REPEAT_NONE:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_ALL);
                        break;
                    case MusicPlaybackService.REPEAT_ALL:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_CURRENT);
                        if (mService.getShuffleMode() != MusicPlaybackService.SHUFFLE_NONE) {
                            mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
                        }
                        break;
                    default:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_NONE);
                        break;
                }
            }
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * Cycles through the shuffle options.
     */
    public static void cycleShuffle() {
        try {
            if (mService != null) {
                switch (mService.getShuffleMode()) {
                    case MusicPlaybackService.SHUFFLE_NONE:
                        mService.setShuffleMode(MusicPlaybackService.SHUFFLE);
                        if (mService.getRepeatMode() == MusicPlaybackService.REPEAT_CURRENT) {
                            mService.setRepeatMode(MusicPlaybackService.REPEAT_ALL);
                        }
                        break;
                    case MusicPlaybackService.SHUFFLE:
                        mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
                        break;
                    default:
                        break;
                }
            }
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * @return True if we're playing music, false otherwise.
     */
    public static boolean isPlaying() {
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (final RemoteException ignored) {
            }
        }
        return false;
    }

    /**
     * @return The current shuffle mode.
     */
    public static int getShuffleMode() {
        if (mService != null) {
            try {
                return mService.getShuffleMode();
            } catch (final RemoteException ignored) {
            }
        }

        return 0;
    }

    /**
     * @return The current repeat mode.
     */
    public static int getRepeatMode() {
        if (mService != null) {
            try {
                return mService.getRepeatMode();
            } catch (final RemoteException ignored) {
            }
        }

        return 0;
    }

    public static Audio getCurrentAudio(){
        if (mService != null) {
            try {
                return mService.getCurrentAudio();
            } catch (final RemoteException ignored) {
            }
        }

        return null;
    }

    /**
     * @return The current track name.
     */
    public static String getTrackName() {
        if (mService != null) {
            try {
                return mService.getTrackName();
            } catch (final RemoteException ignored) {
            }
        }

        return null;
    }

    /**
     * @return The current artist name.
     */
    public static String getArtistName() {
        if (mService != null) {
            try {
                return mService.getArtistName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    /**
     * @return The current song Id.
     */
    public static int getAudioSessionId() {
        if (mService != null) {
            try {
                return mService.getAudioSessionId();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    /**
     * @return The queue.
     */
    public static List<Audio> getQueue() {
        try {
            if (mService != null) {
                return mService.getQueue();
            }
        } catch (final RemoteException ignored) {
        }

        return Collections.emptyList();
    }


    /**
     * @param context The {@link Context} to use
     * @param audio   The source of the file
     */
    public static void playFile(final Context context, final Audio audio) {
        if (audio == null || mService == null) {
            return;
        }

        try {
            mService.stop();
            mService.openFile(audio);
            mService.play();
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * Called when one of the lists should refresh or requery.
     */
    public static void refresh() {
        try {
            if (mService != null) {
                mService.refresh();
            }
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * Seeks the current track to a desired position
     *
     * @param position The position to seek to
     */
    public static void seek(final long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    /**
     * @return The current position time of the track
     */
    public static long position() {
        if (mService != null) {
            try {
                return mService.position();
            } catch (final RemoteException ignored) {
            }
        }

        return 0;
    }

    /**
     * @return The total length of the current track
     */
    public static long duration() {
        if (mService != null) {
            try {
                return mService.duration();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    /**
     * @return The total length of the current track
     */
    public static int bufferPercent() {
        if (mService != null) {
            try {
                return mService.getBufferPercent();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    private static final String TAG = MusicUtils.class.getSimpleName();

    /**
     * Used to build and show a notification when Apollo is sent into the
     * background
     *
     * @param context The {@link Context} to use.
     */
    public static void notifyForegroundStateChanged(final Context context, boolean inForeground) {
        int old = sForegroundActivities;
        if (inForeground) {
            sForegroundActivities++;
        } else {
            sForegroundActivities--;
        }

        if (old == 0 || sForegroundActivities == 0) {
            boolean nowInForeground = sForegroundActivities != 0;
            Logger.d(TAG, "notifyForegroundStateChanged, nowInForeground: " + nowInForeground);

            final Intent intent = new Intent(context, MusicPlaybackService.class);
            intent.setAction(MusicPlaybackService.FOREGROUND_STATE_CHANGED);
            intent.putExtra(MusicPlaybackService.NOW_IN_FOREGROUND, nowInForeground);
            context.startService(intent);
        }
    }

}
