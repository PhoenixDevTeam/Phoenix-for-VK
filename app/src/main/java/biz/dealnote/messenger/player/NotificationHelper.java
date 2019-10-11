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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.longpoll.AppNotificationChannels;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Utils;

public class NotificationHelper {

    private static final int PHOENIX_MUSIC_SERVICE = 1;
    private final NotificationManager mNotificationManager;
    private final MusicPlaybackService mService;
    private NotificationCompat.Builder mNotificationBuilder = null;

    private static final int ACTION_PLAY_PAUSE = 1;
    private static final int ACTION_NEXT = 2;
    private static final int ACTION_PREV = 3;


    public NotificationHelper(final MusicPlaybackService service) {
        mService = service;
        mNotificationManager = (NotificationManager) service
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification(Context context, final String artistName, final String trackName,
                                  final boolean isPlaying, final Bitmap cover, MediaSessionCompat.Token mediaSessionToken) {

        if (Utils.hasOreo()) {
            mNotificationManager.createNotificationChannel(AppNotificationChannels.getAudioChannel(context));
        }

        // Notification Builder
        mNotificationBuilder = new NotificationCompat.Builder(mService, AppNotificationChannels.AUDIO_CHANNEL_ID)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.itunes)
                .setContentTitle(artistName)
                .setContentText(trackName)
                .setContentIntent(getOpenIntent(context))
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(cover)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSessionToken)
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0, 1, 2)
                        .setCancelButtonIntent(retreivePlaybackActions(4)))
                .addAction(new NotificationCompat.Action(R.drawable.prev_notification,
                        context.getResources().getString(R.string.previous),
                        retreivePlaybackActions(ACTION_PREV)))
                .addAction(new NotificationCompat.Action(isPlaying ? R.drawable.pause_notification : R.drawable.play_notification,
                        context.getResources().getString(isPlaying ? R.string.pause : R.string.play),
                        retreivePlaybackActions(ACTION_PLAY_PAUSE)))
                .addAction(new NotificationCompat.Action(R.drawable.next_notification,
                        context.getResources().getString(R.string.next),
                        retreivePlaybackActions(ACTION_NEXT)));

        mService.startForeground(PHOENIX_MUSIC_SERVICE, mNotificationBuilder.build());
    }

    public void killNotification() {
        mService.stopForeground(true);
        mNotificationBuilder = null;
    }

    @SuppressLint("RestrictedApi")
    public void updatePlayState(final boolean isPlaying) {
        if (mNotificationBuilder == null || mNotificationManager == null) {
            return;
        }

        if (!isPlaying) {
            mService.stopForeground(false);
        }
        //Remove pause action
        mNotificationBuilder.mActions.remove(1);
        mNotificationBuilder.mActions.add(1, new NotificationCompat.Action(
                isPlaying ? R.drawable.pause_notification : R.drawable.play_notification,
                null,
                retreivePlaybackActions(1)));

        mNotificationManager.notify(PHOENIX_MUSIC_SERVICE, mNotificationBuilder.build());
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
            case ACTION_PLAY_PAUSE:
                // Play and pause
                action = new Intent(MusicPlaybackService.TOGGLEPAUSE_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 1, action, 0);
                return pendingIntent;
            case ACTION_NEXT:
                // Skip tracks
                action = new Intent(MusicPlaybackService.NEXT_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 2, action, 0);
                return pendingIntent;
            case ACTION_PREV:
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
