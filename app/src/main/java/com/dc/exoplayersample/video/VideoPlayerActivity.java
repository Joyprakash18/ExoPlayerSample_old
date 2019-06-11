package com.dc.exoplayersample.video;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dc.exoplayersample.MainActivity;
import com.dc.exoplayersample.R;
import com.dc.exoplayersample.api.ApiClient;
import com.dc.exoplayersample.api.ApiInterface;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoPlayerActivity extends AppCompatActivity {

    private ConstraintLayout constraintlayout;
    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private ImageView screenRotation;
    private TextView title;
    private TextView description;
    private ImageView controllerLock;
    private ImageView playPause;
    private ImageView back;
    private ConstraintLayout bottomContainer;
    private ConstraintLayout topContainer;
    private ProgressBar progress;
    private ImageView backwardPlayback;
    private ImageView forwardPlayback;
    private ImageView volumeMute;
    private ImageView brightnessVolumeImage;
    private TextView counter;
    private Group counterGroup;
    private TextView increaseSpeed;
    private TextView speedText;
    private TextView decreaseSpeed;
    private Group speedControlGroup;
    private ImageView repeatOnOff;
    private TextView aspectRatio;
    private TextView pictureInPicture;

    private AudioManager audioManager;

    private boolean isControllingVolume = false;
    private boolean isControllingBrightness = false;
    private boolean isPlaying = true;
    private boolean isControlLocked = false;
    private boolean isHorizontalScrolling = false;
    private boolean isVerticalScrolling = false;
    private boolean isControllerVisible = false;
    private boolean isControllingPlayback = false;
    private boolean isVolumeMute = false;
    private boolean isRepeatEnabled = false;
    private boolean iWantToBeInPipModeNow = false;

    private float speed = 0;
    private int brightness = 0;
    private int volume = 0;
    private static final int maxValue = 100;
    private static final int minValue = 0;
    private static int horizontalScrollThreshold = 0;
    private float motionDownXPosition = 0;
    private float motionDownYPosition = 0;
    private int currentAspectRatio = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        findIds();
        setValues();
        clickListener();
        callVideoApi();

    }

    private void setValues() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * (100 / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        try {
            brightness = (((Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS)) / 255) * 100);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        horizontalScrollThreshold = (getScreenHeightWidth()[1] / maxValue) / 2;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void clickListener() {
        screenRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOrientationControl();
            }
        });
        controllerLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLockControl();
            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayPause();
            }
        });
        increaseSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSpeedPlayback(true);
            }
        });
        decreaseSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSpeedPlayback(false);
            }
        });

        backwardPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set10SecForwardBackwardPlayback(false);
            }
        });
        forwardPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set10SecForwardBackwardPlayback(true);
            }
        });
        volumeMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVolumeMute();
            }
        });
        repeatOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepeatOnOff();
            }
        });
        aspectRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAspectRatio();
            }
        });
        pictureInPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPictureInPictureMode();
            }
        });
        simpleExoPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (player != null) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            motionDownXPosition = event.getX();
                            motionDownYPosition = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            handleTouchEvent(event);
                            break;
                        case MotionEvent.ACTION_UP:
                            if (isPlaying) {
                                player.setPlayWhenReady(true);
                            }
                            showHideController(event);
                            resetValues();
                            break;
                    }
                }
                return true;
            }
        });
    }

    private void setPictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enterPictureInPictureMode();
            navToLauncherTask(VideoPlayerActivity.this);
            simpleExoPlayerView.setUseController(false);
        }
    }

    private void setRepeatOnOff() {
        if(isRepeatEnabled){
            repeatOnOff.setImageResource(R.drawable.ic_repeat_off);
        }else{
            repeatOnOff.setImageResource(R.drawable.ic_repeat);
        }
        isRepeatEnabled = !isRepeatEnabled;
    }

    private void setAspectRatio() {
        simpleExoPlayerView.showController();
        currentAspectRatio++;
        if(currentAspectRatio > 4){
            currentAspectRatio = 0;
        }
        switch (currentAspectRatio){
            case 0:
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                aspectRatio.setText("Fit");
                break;
            case 1:
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                aspectRatio.setText("V");
                break;
            case 2:
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
                aspectRatio.setText("H");
                break;
            case 3:
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                aspectRatio.setText("Fill");
                break;
            case 4:
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                aspectRatio.setText("Zoom");
                break;

        }

    }

    private void set10SecForwardBackwardPlayback(boolean isIncreasing) {
        simpleExoPlayerView.showController();
        long currentDuration = player.getContentPosition();
        long totalDuration = player.getDuration();
        if (isIncreasing) {
            currentDuration += 10000;
        } else {
            currentDuration -= 10000;
        }
        if (currentDuration >= totalDuration) {
            currentDuration = totalDuration;
        } else if (currentDuration <= 0) {
            currentDuration = 0;
        }
        player.seekTo(currentDuration);
    }

    private void showHideController(MotionEvent event) {
        if (motionDownXPosition == event.getX() || motionDownYPosition == event.getY()) {
            controllerVisibility();
            if (isControllerVisible) {
                simpleExoPlayerView.hideController();
            } else {
                simpleExoPlayerView.showController();
            }
            isControllerVisible = !isControllerVisible;
        }
    }

    private void setSpeedPlayback(boolean isIncreasing) {
        simpleExoPlayerView.showController();
        float speedThreshold = 0.25f;
        if (player != null) {
            if (isIncreasing) {
                speed += speedThreshold;
            } else {
                speed -= speedThreshold;
            }
            player.setPlaybackParameters(new PlaybackParameters(speed));
            speedText.setText(String.format("%s x", String.format(Locale.ENGLISH, "%.2f", speed)));
        }
    }

    private void setVolumeMute() {
        simpleExoPlayerView.showController();
        if (isVolumeMute) {
            player.setVolume(1f);
            volumeMute.setImageResource(R.drawable.ic_volume_up);
        } else {
            player.setVolume(0f);
            volumeMute.setImageResource(R.drawable.ic_volume_off);
        }
        isVolumeMute = !isVolumeMute;
    }

    private void handleTouchEvent(MotionEvent event) {
        if (!isControlLocked) {
            if (!isHorizontalScrolling && !isVerticalScrolling) {
                if (motionDownYPosition - event.getY() > 50 || event.getY() - motionDownYPosition > 50) {
                    isHorizontalScrolling = true;
                    isVerticalScrolling = false;
                } else if (motionDownXPosition - event.getX() > 50 || event.getX() - motionDownXPosition > 50) {
                    isHorizontalScrolling = false;
                    isVerticalScrolling = true;
                }
            }
            if (isHorizontalScrolling) {
                if (event.getX() > getScreenHeightWidth()[0] / 2) { //right
                    isControllingVolume = true;
                    controlVolume(event);
                } else if (event.getX() < getScreenHeightWidth()[0] / 2) {  //left
                    isControllingBrightness = true;
                    controlBrightness(event);
                }
            } else if (isVerticalScrolling) {
                isControllingPlayback = true;
                controlPlayback(event);
            }
        }

    }

    private void callVideoApi() {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<VideoResponse> call = apiInterface.getVideo();
        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VideoResponse> call, @NonNull Response<VideoResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        setDataToFields(response.body());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<VideoResponse> call, @NonNull Throwable t) {
            }
        });
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

    private void setOrientationControl() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    private void setLockControl() {
        if (isControlLocked) {
            controllerLock.setImageResource(R.drawable.ic_lock);
        } else {
            controllerLock.setImageResource(R.drawable.ic_unlock);
        }
        isControlLocked = !isControlLocked;
        controllerVisibility();
    }

    private void controllerVisibility() {
        if (isControllingVolume) {
            brightnessVolumeImage.setImageResource(R.drawable.ic_volume_up);
            counterGroup.setVisibility(View.VISIBLE);
            speedControlGroup.setVisibility(View.GONE);
            topContainer.setVisibility(View.GONE);
            bottomContainer.setVisibility(View.GONE);
        } else if (isControllingBrightness) {
            brightnessVolumeImage.setImageResource(R.drawable.ic_brightness);
            counterGroup.setVisibility(View.VISIBLE);
            speedControlGroup.setVisibility(View.GONE);
            topContainer.setVisibility(View.GONE);
            bottomContainer.setVisibility(View.GONE);
        } else if (isControllingPlayback) {
            brightnessVolumeImage.setImageResource(0);
            counterGroup.setVisibility(View.VISIBLE);
            speedControlGroup.setVisibility(View.GONE);
            topContainer.setVisibility(View.GONE);
            bottomContainer.setVisibility(View.GONE);
        } else {
            if (isControlLocked) {
                bottomContainer.setVisibility(View.GONE);
                topContainer.setVisibility(View.VISIBLE);
                topContainer.setBackground(null);
                back.setVisibility(View.GONE);
                title.setVisibility(View.GONE);
                volumeMute.setVisibility(View.GONE);
                counterGroup.setVisibility(View.GONE);
                speedControlGroup.setVisibility(View.GONE);
                controllerLock.setVisibility(View.VISIBLE);
            } else {
                bottomContainer.setVisibility(View.VISIBLE);
                topContainer.setVisibility(View.VISIBLE);
                topContainer.setBackground(getResources().getDrawable(R.drawable.video_controller_gradiant_background_top));
                back.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                volumeMute.setVisibility(View.VISIBLE);
                counterGroup.setVisibility(View.GONE);
                speedControlGroup.setVisibility(View.VISIBLE);
                controllerLock.setVisibility(View.VISIBLE);
            }
        }
    }

    private void controlVolume(MotionEvent event) {
        int newVolume = volume;

        newVolume = getNewSwipedValue(event, volume, newVolume, 1, horizontalScrollThreshold);
        if (newVolume >= maxValue) {
            newVolume = maxValue;
        } else if (newVolume <= minValue) {
            newVolume = minValue;
        }
        int convertedNewVolume = (newVolume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, convertedNewVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        counter.setText(String.valueOf(newVolume));
        volume = newVolume;
    }

    private void controlBrightness(MotionEvent event) {
        int newBrightness = brightness;
        newBrightness = getNewSwipedValue(event, brightness, newBrightness, 1, horizontalScrollThreshold);
        if (newBrightness >= maxValue) {
            newBrightness = maxValue;
        } else if (newBrightness <= minValue) {
            newBrightness = minValue;
        }
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = (float) newBrightness / 100;
        getWindow().setAttributes(layoutParams);
        counter.setText(String.valueOf(newBrightness));
        brightness = newBrightness;

    }

    private void controlPlayback(MotionEvent event) {
        player.setPlayWhenReady(false);
        long currentDuration = player.getContentPosition();
        long totalDuration = player.getDuration();
        float newDuration = currentDuration;
        controllerVisibility();
        simpleExoPlayerView.showController();
        int verticalScrollThreshold = 500;
        if (motionDownXPosition > event.getX()) { //swiped left
            newDuration = currentDuration - verticalScrollThreshold;
        } else if (motionDownXPosition < event.getX()) { //swiped right
            newDuration = currentDuration + verticalScrollThreshold;
        }
        motionDownXPosition = event.getX();
        if (newDuration >= totalDuration) {
            newDuration = totalDuration;
        } else if (newDuration <= 0) {
            newDuration = 0;
        }
        player.seekTo((long) newDuration);

        long hours = TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition()));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()));
        String milliSecToHMS = String.format(Locale.ENGLISH, "%02d.%02d.%02d", hours, minutes, seconds);
        counter.setText(milliSecToHMS);

    }

    private int getNewSwipedValue(MotionEvent event, int value, int newValue, int step, int threshold) {
        int swipeDifference;
        controllerVisibility();
        if (motionDownYPosition > event.getY()) { // swiped up
            swipeDifference = (int) (motionDownYPosition - event.getY());
            if (swipeDifference > threshold) {
                newValue = value + step;
                motionDownYPosition = event.getY();
                simpleExoPlayerView.showController();
            }
        } else if (motionDownYPosition < event.getY()) {  //swiped down
            swipeDifference = (int) (event.getY() - motionDownYPosition);
            if (swipeDifference > threshold) {
                newValue = value - step;
                motionDownYPosition = event.getY();
                simpleExoPlayerView.showController();
            }
        }
        return newValue;
    }

    private void resetValues() {
        motionDownXPosition = 0;
        motionDownYPosition = 0;
        isControllingVolume = false;
        isControllingBrightness = false;
        isHorizontalScrolling = false;
        isVerticalScrolling = false;
        isControllingPlayback = false;
    }

    private void setDataToFields(VideoResponse body) {
        initializePlayer(body.getSources());
        description.setText(body.getDescription());
        title.setText(body.getTitle());
        if (player != null) {
            speed = player.getPlaybackParameters().speed;
            speedText.setText(String.format("%s x", String.format(Locale.ENGLISH, "%.2f", speed)));
        }
    }

    private void setPlayerConstraints() {
        int orientation = getResources().getConfiguration().orientation;
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintlayout);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
            set.connect(simpleExoPlayerView.getId(), ConstraintSet.BOTTOM, constraintlayout.getId(), ConstraintSet.BOTTOM, 0);
            set.constrainHeight(R.id.simpleExoPlayerView, 0);
            set.applyTo(constraintlayout);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            showSystemUI();
            set.connect(simpleExoPlayerView.getId(), ConstraintSet.TOP, constraintlayout.getId(), ConstraintSet.TOP, 0);
            set.clear(R.id.simpleExoPlayerView, ConstraintSet.BOTTOM);
            int heightDpToPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());
            set.constrainHeight(R.id.simpleExoPlayerView, heightDpToPx);
            set.applyTo(constraintlayout);
        }
    }

    private void initializePlayer(String sources) {
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setUseController(true);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exo-player"));
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(sources));
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
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
                        if(isRepeatEnabled){
                            player.seekTo(0);
                        }
                        break;
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_READY:
                        progress.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public static void navToLauncherTask(Context appContext) {
        ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        final List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
        for (ActivityManager.AppTask task : appTasks) {
            final Intent baseIntent = task.getTaskInfo().baseIntent;
            final Set<String> categories = baseIntent.getCategories();
            if (categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
                task.finishAndRemoveTask();
                return;
            }
        }
    }

    private void findIds() {
        constraintlayout = findViewById(R.id.constraintLayout);
        simpleExoPlayerView = findViewById(R.id.simpleExoPlayerView);
        description = findViewById(R.id.description);
        progress = findViewById(R.id.progress);

        bottomContainer = simpleExoPlayerView.findViewById(R.id.bottomContainer);
        screenRotation = simpleExoPlayerView.findViewById(R.id.screenRotation);
        backwardPlayback = simpleExoPlayerView.findViewById(R.id.backwardPlayback);
        playPause = simpleExoPlayerView.findViewById(R.id.playPause);
        forwardPlayback = simpleExoPlayerView.findViewById(R.id.forwardPlayback);
        repeatOnOff = simpleExoPlayerView.findViewById(R.id.repeatOnOff);
        aspectRatio = simpleExoPlayerView.findViewById(R.id.aspectRatio);
        pictureInPicture = simpleExoPlayerView.findViewById(R.id.pictureInPicture);

        topContainer = simpleExoPlayerView.findViewById(R.id.topContainer);
        back = simpleExoPlayerView.findViewById(R.id.back);
        title = simpleExoPlayerView.findViewById(R.id.title);
        controllerLock = simpleExoPlayerView.findViewById(R.id.controllerLock);
        volumeMute = simpleExoPlayerView.findViewById(R.id.volumeMute);


        brightnessVolumeImage = simpleExoPlayerView.findViewById(R.id.brightnessVolumeImage);
        counter = simpleExoPlayerView.findViewById(R.id.counter);
        counterGroup = simpleExoPlayerView.findViewById(R.id.counterGroup);

        increaseSpeed = simpleExoPlayerView.findViewById(R.id.increaseSpeed);
        speedText = simpleExoPlayerView.findViewById(R.id.speedText);
        decreaseSpeed = simpleExoPlayerView.findViewById(R.id.decreaseSpeed);
        speedControlGroup = simpleExoPlayerView.findViewById(R.id.speedControlGroup);

    }

    private int[] getScreenHeightWidth() {
        int[] heightWidth = new int[2];
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        heightWidth[0] = size.x;  //width
        heightWidth[1] = size.y;  //height
        return heightWidth;
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setPlayerConstraints();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onUserLeaveHint() {
        setPictureInPictureMode();
    }

    @Override
    protected void onPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (player != null) {
                if(isInPictureInPictureMode()){
                    player.setPlayWhenReady(isPlaying);
                }else{
                    player.setPlayWhenReady(false);
                    player = null;
                }
            }
        }else{
            if (player != null) {
                player.setPlayWhenReady(false);
                player = null;
            }
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
            simpleExoPlayerView.setUseController(true);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
        }
    }

}
