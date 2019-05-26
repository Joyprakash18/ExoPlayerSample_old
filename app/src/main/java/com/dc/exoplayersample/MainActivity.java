package com.dc.exoplayersample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dc.exoplayersample.adaptivestreaming.AdaptiveStreamingActivity;
import com.dc.exoplayersample.audio.AudioPlayerActivity;
import com.dc.exoplayersample.video.VideoPlayerActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.audioPlayerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AudioPlayerActivity.class));
            }
        });
        findViewById(R.id.videoPlayerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VideoPlayerActivity.class));
            }
        });

        findViewById(R.id.adaptiveStreamPlayerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AdaptiveStreamingActivity.class));
            }
        });

    }


}
