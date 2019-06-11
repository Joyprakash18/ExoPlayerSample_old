package com.dc.exoplayersample.adaptivestreaming;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dc.exoplayersample.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class AdaptiveStreamingActivity extends AppCompatActivity {

    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    String hls_url = "https://playertest.longtailvideo.com/adaptive/bbbfull/bbbfull.m3u8";
    private ProgressBar progress;
    private boolean isPlaying = true;
    private ImageView playPause;
    private DefaultTrackSelector defaultTrackSelector;
    private ArrayList<Format> qualityList;
    private ImageView setting;
    private TrackGroupArray trackGroups;
    private PopupMenu popupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adaptive_streaming);

        findIds();
        clickListener();
        initializePlayer();
    }

    private void initializePlayer() {
        TrackSelection.Factory adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        defaultTrackSelector = new DefaultTrackSelector(adaptiveTrackSelection);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(AdaptiveStreamingActivity.this),
                defaultTrackSelector,
                new DefaultLoadControl());
        simpleExoPlayerView.setPlayer(player);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                this,
                Util.getUserAgent(this, "exo-player"),
                new DefaultBandwidthMeter());
        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(hls_url));
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        progress.setVisibility(View.VISIBLE);
                        break;
                    case Player.STATE_ENDED:
                        player.seekTo(0);
                        break;
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_READY:
                        progress.setVisibility(View.GONE);
                        getVideoQuality();
                        break;
                    default:
                        break;
                }
            }
        });


    }

    private void getVideoQuality() {
        if (defaultTrackSelector.getCurrentMappedTrackInfo() != null) {
            qualityList = new ArrayList<>();
            // 0 => video 1 => Audio
            trackGroups = defaultTrackSelector.getCurrentMappedTrackInfo().getTrackGroups(0);
            for (int i = 0; i < trackGroups.get(0).length; i++) {
                Format quality = trackGroups.get(0).getFormat(i);
                qualityList.add(quality);
            }
            setPopupMenu();
        }
    }

    private void setPopupMenu(){
        if (qualityList != null && qualityList.size() > 0) {
            popupMenu = new PopupMenu(AdaptiveStreamingActivity.this, setting);
            popupMenu.getMenu().add(1, 0, 1, "Auto");
            for (int i = 0; i < qualityList.size(); i++) {
                popupMenu.getMenu().add(1, i + 1, i + 2, String.valueOf(qualityList.get(i).height));
            }
        }
    }

    private void setPlayPause() {
        if (isPlaying) {
            player.setPlayWhenReady(false);
            playPause.setImageResource(R.drawable.ic_play);
        } else {
            player.setPlayWhenReady(true);
            playPause.setImageResource(R.drawable.ic_pause);
        }
        isPlaying = !isPlaying;
    }

    private void clickListener() {
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayPause();
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popupMenu != null){
                    popupMenu.show();
                }
            }
        });

        if (popupMenu != null) {
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == 0) {
                        Toast.makeText(AdaptiveStreamingActivity.this, "auto", Toast.LENGTH_SHORT).show();
                    } else {
                        if (defaultTrackSelector != null) {
                            int bitrate = qualityList.get(item.getItemId() - 1).bitrate;
                            defaultTrackSelector.setParameters(defaultTrackSelector.buildUponParameters()
                                    .setMaxVideoBitrate(bitrate)
                                    .build()
                            );
                        }
                    }
                    return false;
                }
            });
        }

    }

    private void findIds() {
        simpleExoPlayerView = findViewById(R.id.simpleExoPlayerView);
        progress = simpleExoPlayerView.findViewById(R.id.progress);
        playPause = simpleExoPlayerView.findViewById(R.id.playPause);
        setting = simpleExoPlayerView.findViewById(R.id.setting);
    }

    @Override
    protected void onPause() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player = null;
        }
        super.onPause();
    }
}
