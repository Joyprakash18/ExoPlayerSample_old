package com.dc.exoplayersample.audio;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dc.exoplayersample.R;
import com.dc.exoplayersample.api.ApiClient;
import com.dc.exoplayersample.api.ApiInterface;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudioPlayerActivity extends AppCompatActivity {

    private SimpleExoPlayer player;
    private ImageView back;
    private TextView title;
    private ImageView thumbnail;
    private ImageView playPause;
    private ImageView repeatOnOff;
    private PlayerView playerView;

    private boolean isPlaying = true;
    private boolean isRepeatEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        findIDs();
        clickListener();
        callVideoApi();
    }

    private void setRepeatOnOff() {
        if (isRepeatEnabled) {
            repeatOnOff.setImageResource(R.drawable.ic_repeat_off_black);
        } else {
            repeatOnOff.setImageResource(R.drawable.ic_repeat_black);
        }
        isRepeatEnabled = !isRepeatEnabled;
    }

    private void setPlayPause() {
        if (isPlaying) {
            player.setPlayWhenReady(false);
            playPause.setImageResource(R.drawable.ic_play_black);
        } else {
            player.setPlayWhenReady(true);
            playPause.setImageResource(R.drawable.ic_pause_black);
        }
        isPlaying = !isPlaying;
    }

    private void setDataToFields(AudioResponse body) {
        initializePlayer(body.getSources());
        title.setText(body.getTitle());
        Glide.with(AudioPlayerActivity.this).load(body.getThumb()).into(thumbnail);
    }

    private void initializePlayer(String source) {
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        playerView.setPlayer(player);
        playerView.setControllerShowTimeoutMs(0);
        playerView.setControllerHideOnTouch(false);
        DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "AudioDemo"));
        MediaSource mediaSource = new ExtractorMediaSource.Factory(defaultDataSourceFactory).createMediaSource(Uri.parse(source));
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:

                        break;
                    case Player.STATE_ENDED:
                        if (isRepeatEnabled) {
                            player.seekTo(0);
                        }
                        break;
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_READY:
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void callVideoApi() {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<AudioResponse> call = apiInterface.getAudio();
        call.enqueue(new Callback<AudioResponse>() {
            @Override
            public void onResponse(@NonNull Call<AudioResponse> call, @NonNull Response<AudioResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        setDataToFields(response.body());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AudioResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void clickListener() {
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayPause();
            }
        });

        repeatOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepeatOnOff();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void findIDs() {
        playerView = findViewById(R.id.simpleExoPlayerView);
        back = playerView.findViewById(R.id.back);
        title = playerView.findViewById(R.id.title);
        thumbnail = playerView.findViewById(R.id.thumbnail);
        playPause = playerView.findViewById(R.id.playPause);
        repeatOnOff = playerView.findViewById(R.id.repeatOnOff);
    }

}
