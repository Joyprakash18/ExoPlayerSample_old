package com.dc.exoplayersample.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dc.exoplayersample.R;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


public class AudioPlayerService extends Service {

    private SimpleExoPlayer player;
    private PlayerNotificationManager playerNoticiationManager;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    private void initializePlayer(String source, final String title, String thumbnail) {
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "AudioDemo"));
        MediaSource mediaSource = new ExtractorMediaSource.Factory(defaultDataSourceFactory).createMediaSource(Uri.parse(source));
        player.prepare(mediaSource);
        //player.setPlayWhenReady(true);

        playerNoticiationManager = PlayerNotificationManager.createWithNotificationChannel(
                getBaseContext(), "200", R.string.audio_player_name, 1,
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public String getCurrentContentTitle(Player player) {
                        return title;
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        Intent intent = new Intent(getBaseContext(), AudioPlayerActivity.class);
                        return PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    @Nullable
                    @Override
                    public String getCurrentContentText(Player player) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        return null;
                    }
                });
        playerNoticiationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });

        playerNoticiationManager.setPlayer(player);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("data")) {
            Bundle data = intent.getBundleExtra("data");
            String source = data.getString("source");
            String title = data.getString("title");
            String thumbnail = data.getString("thumbnail");

            initializePlayer(source, title, thumbnail);

        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        playerNoticiationManager.setPlayer(null);
        player.release();
        player = null;
        super.onDestroy();
    }
}
