package com.github.yoojia.versiontest.api.entity;

import com.github.yoojia.anyversion.Version;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Andy on 4/7/2016.
 */
public class VersionInfo {

    @SerializedName("versionName")
    @Expose
    private String versionName;
    @SerializedName("note")
    @Expose
    private String note;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("versionCode")
    @Expose
    private int versionCode;

    public VersionInfo() {
    }

    public Version createVersionObj() {
        return new Version(versionName, note, url, versionCode);
    }

}
