package com.dc.exoplayersample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dc.exoplayersample.adaptivestreaming.AdaptiveStreamingActivity;
import com.dc.exoplayersample.api.ApiClient;
import com.dc.exoplayersample.api.ApiInterface;
import com.dc.exoplayersample.audio.AudioPlayerActivity;
import com.dc.exoplayersample.video.VideoActivity;
import com.dc.exoplayersample.video.VideoPlayerActivity;
import com.dc.exoplayersample.video.VideoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
                callVideoApi();
//                startActivity(new Intent(MainActivity.this, VideoPlayerActivity.class));
            }
        });

        findViewById(R.id.adaptiveStreamPlayerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AdaptiveStreamingActivity.class));
            }
        });

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

    private void setDataToFields(VideoResponse body) {
        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
        intent.putExtra("VideoActivity.URL",""+body.getSources());
//        intent.putExtra("VideoActivity.URL","https://www.youtube.com/watch?v=C0DPdy98e4c");
        startActivity(intent);
    }


}
