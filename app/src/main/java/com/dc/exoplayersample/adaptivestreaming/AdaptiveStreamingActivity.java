package com.dc.exoplayersample.adaptivestreaming;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
    String hls_url = "https://playertest.longtailvideo.com/adaptive/bbbfull/bbbfull.m3u8";

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
        simpleExoPlayerView.setPlayer(player);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                this,
                Util.getUserAgent(this, "exo-player"),
                new DefaultBandwidthMeter());
        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(hls_url));
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }

    private void findIds() {
        simpleExoPlayerView = findViewById(R.id.simpleExoPlayerView);
    }
}
