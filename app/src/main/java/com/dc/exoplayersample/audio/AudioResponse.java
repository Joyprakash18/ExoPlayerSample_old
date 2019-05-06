package com.dc.exoplayersample.audio;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AudioResponse {
    @SerializedName("sources")
    @Expose
    private String sources;
    @SerializedName("thumb")
    @Expose
    private String thumb;
    @SerializedName("title")
    @Expose
    private String title;

    public String getSources() {
        return sources;
    }

    public String getThumb() {
        return thumb;
    }

    public String getTitle() {
        return title;
    }
}
