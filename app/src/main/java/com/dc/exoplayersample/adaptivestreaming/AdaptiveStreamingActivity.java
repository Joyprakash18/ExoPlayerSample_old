package com.dc.exoplayersample.adaptivestreaming;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    private Button hlsButton;
    private Button dashButton;
    private Button smoothStreamButton;
    private String lastButtonSelected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adaptive_streaming);

        findIds();
        clickListener();
        setCurrentVideo("HLS");
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
        }
    }

    private void showPopupMenu(){
        if (qualityList != null && qualityList.size() > 0) {
            popupMenu = new PopupMenu(AdaptiveStreamingActivity.this, setting);
            popupMenu.getMenu().add(1, 0, 1, "Auto");
            for (int i = 0; i < qualityList.size(); i++) {
                popupMenu.getMenu().add(1, i + 1, i + 2, String.valueOf(qualityList.get(i).height));
            }
            popupMenu.show();

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
                showPopupMenu();
            }
        });

        hlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastButtonSelected != null && !lastButtonSelected.equals("HLS")) {
                    setCurrentVideo("HLS");
                }
            }
        });

        dashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastButtonSelected != null && !lastButtonSelected.equals("DASH")) {
                    setCurrentVideo("DASH");
                }
            }
        });

        smoothStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastButtonSelected != null && !lastButtonSelected.equals("SMOOTHSTREAM")) {
                    setCurrentVideo("SMOOTHSTREAM");
                }
            }
        });

    }

    private void setCurrentVideo(String currentVideoPlaying) {
        setButtonEnabled(currentVideoPlaying);
        lastButtonSelected = currentVideoPlaying;
        switch (currentVideoPlaying) {
            case "HLS":
                initializeHLSPlayer();
                break;
            case "DASH":
                initializeDASHPlayer();
                break;
            case "SMOOTHSTREAM":
                break;
            default:
                break;
        }
    }

    private void initializeDASHPlayer() {

    }

    private void initializeHLSPlayer() {
        clearPLayer();
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

    private void setButtonEnabled(String currentVideoPlaying) {
        switch (currentVideoPlaying) {
            case "HLS":
                hlsButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                hlsButton.setTextColor(getResources().getColor(R.color.colorWhite));
                dashButton.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                dashButton.setTextColor(getResources().getColor(R.color.colorBlack));
                smoothStreamButton.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                smoothStreamButton.setTextColor(getResources().getColor(R.color.colorBlack));
                break;
            case "DASH":
                hlsButton.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                hlsButton.setTextColor(getResources().getColor(R.color.colorBlack));
                dashButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                dashButton.setTextColor(getResources().getColor(R.color.colorWhite));
                smoothStreamButton.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                smoothStreamButton.setTextColor(getResources().getColor(R.color.colorBlack));
                break;
            case "SMOOTHSTREAM":
                dashButton.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                dashButton.setTextColor(getResources().getColor(R.color.colorBlack));
                dashButton.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                dashButton.setTextColor(getResources().getColor(R.color.colorBlack));
                smoothStreamButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                smoothStreamButton.setTextColor(getResources().getColor(R.color.colorWhite));
                break;
            default:
                break;
        }
    }

    private void clearPLayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            defaultTrackSelector = null;
            player = null;
        }
    }

    private void findIds() {
        simpleExoPlayerView = findViewById(R.id.simpleExoPlayerView);
        progress = findViewById(R.id.progress);
        hlsButton = findViewById(R.id.hlsButton);
        dashButton = findViewById(R.id.dashButton);
        smoothStreamButton = findViewById(R.id.smoothStreamButton);

        playPause = simpleExoPlayerView.findViewById(R.id.playPause);
        setting = simpleExoPlayerView.findViewById(R.id.setting);
    }

    @Override
    protected void onPause() {
        clearPLayer();
        super.onPause();
    }
}
