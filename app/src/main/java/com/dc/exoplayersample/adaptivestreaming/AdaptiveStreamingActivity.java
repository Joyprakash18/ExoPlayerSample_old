package com.dc.exoplayersample.adaptivestreaming;

import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.dc.exoplayersample.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class AdaptiveStreamingActivity extends AppCompatActivity {

    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adaptive_streaming);

        findIds();
        initializePlayer();
    }

    private void initializePlayer() {
        TrackSelection.Factory adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(AdaptiveStreamingActivity.this),
                new DefaultTrackSelector(adaptiveTrackSelection),
                new DefaultLoadControl());

        //init the player
        simpleExoPlayerView.setPlayer(player);

        //-------------------------------------------------
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(AdaptiveStreamingActivity.this,
                Util.getUserAgent(AdaptiveStreamingActivity.this, "Exo2"), defaultBandwidthMeter);

        //-----------------------------------------------
        //Create media source
        String hls_url = "http://playertest.longtailvideo.com/adaptive/bbbfull/bbbfull.m3u8";
        Uri uri = Uri.parse(hls_url);
        Handler mainHandler = new Handler();
        //MediaSource mediaSource = new HlsMediaSource(uri, dataSourceFactory, mainHandler, null);
        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        player.prepare(mediaSource);


        player.setPlayWhenReady(true);
    }

    private void findIds() {
        simpleExoPlayerView = findViewById(R.id.simpleExoPlayerView);
    }
}
