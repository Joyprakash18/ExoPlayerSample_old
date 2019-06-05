package com.dc.exoplayersample.api;

import com.dc.exoplayersample.audio.AudioResponse;
import com.dc.exoplayersample.video.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("a1k1v")
    Call<VideoResponse> getVideo();

    @GET("th3to")
    Call<AudioResponse> getAudio();
}
