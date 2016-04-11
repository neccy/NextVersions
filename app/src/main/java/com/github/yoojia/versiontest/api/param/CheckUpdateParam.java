package com.github.yoojia.versiontest.api.param;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Andy on 3/8/2016.
 */
public class CheckUpdateParam {
    @SerializedName("versionCode")
    @Expose
    private int versionCode;

    /**
     * 仓库全局id
     */
    @SerializedName("warehouseId")
    @Expose
    private String warehouseId;

    public CheckUpdateParam(int versionCode, String warehouseId) {
        this.versionCode = versionCode;
        this.warehouseId = warehouseId;
    }
}
