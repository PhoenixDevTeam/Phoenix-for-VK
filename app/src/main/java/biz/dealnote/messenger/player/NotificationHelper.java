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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.longpoll.AppNotificationChannels;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Utils;

/**
 * Builds the notification for Apollo's service. Jelly Bean and higher uses the
 * expanded notification by default.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class NotificationHelper {

    private static final int APOLLO_MUSIC_SERVICE = 1;
    private final NotificationManager mNotificationManager;
    private final MusicPlaybackService mService;
    private NotificationCompat.Builder mNotificationBuilder = null;


    public NotificationHelper(final MusicPlaybackService service) {
        mService = service;
        mNotificationManager = (NotificationManager) service
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification(Context context, final String albumName, final String artistName,
                                  final String trackName, final Long albumId, final Bitmap albumArt,
                                  final boolean isPlaying, MediaSessionCompat.Token mediaSessionToken) {

        if (Utils.hasOreo()){
            mNotificationManager.createNotificationChannel(AppNotificationChannels.getAudioChannel(context));
        }
        // Notification Builder
        mNotificationBuilder = new NotificationCompat.Builder(mService, AppNotificationChannels.AUDIO_CHANNEL_ID)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.itunes)
                .setContentTitle(artistName)
                .setContentText(trackName)
                .setContentIntent(getOpenIntent(context))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.cover))
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSessionToken)
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0, 1, 2)
                        .setCancelButtonIntent(retreivePlaybackActions(4)))
                .addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.page_first, ""
                        , retreivePlaybackActions(3)))
                .addAction(new android.support.v4.app.NotificationCompat.Action(isPlaying ? R.drawable.pause : R.drawable.play, ""
                        , retreivePlaybackActions(1)))
                .addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.page_last, ""
                        , retreivePlaybackActions(2)));

        mService.startForeground(APOLLO_MUSIC_SERVICE, mNotificationBuilder.build());
    }

    ;

    /**
     * Remove notification
     */
    public void killNotification() {
        mService.stopForeground(true);
        mNotificationBuilder = null;
    }

    /**
     * Changes the playback controls in and out of a paused state
     *
     * @param isPlaying True if music is playing, false otherwise
     */
    public void updatePlayState(final boolean isPlaying) {
        if (mNotificationBuilder == null || mNotificationManager == null) {
            return;
        }

        if (!isPlaying) {
            mService.stopForeground(false);
        }
        //Remove pause action
        mNotificationBuilder.mActions.remove(1);
        mNotificationBuilder.mActions.add(1, new android.support.v4.app.NotificationCompat.Action(isPlaying ? R.drawable.pause : R.drawable.play, ""
                , retreivePlaybackActions(1)));

        mNotificationManager.notify(APOLLO_MUSIC_SERVICE, mNotificationBuilder.build());
    }

    private PendingIntent getOpenIntent(Context context) {
        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getPlayerPlace(aid));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        return PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private PendingIntent retreivePlaybackActions(final int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(mService, MusicPlaybackService.class);
        switch (which) {
            case 1:
                // Play and pause
                action = new Intent(MusicPlaybackService.TOGGLEPAUSE_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 1, action, 0);
                return pendingIntent;
            case 2:
                // Skip tracks
                action = new Intent(MusicPlaybackService.NEXT_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 2, action, 0);
                return pendingIntent;
            case 3:
                // Previous tracks
                action = new Intent(MusicPlaybackService.PREVIOUS_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 3, action, 0);
                return pendingIntent;
            case 4:
                // Stop and collapse the notification
                action = new Intent(MusicPlaybackService.STOP_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 4, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }
}
