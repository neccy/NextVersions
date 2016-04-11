package com.github.yoojia.versiontest.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Andy on 4/7/2016.
 */
public class BaseEntity<T> {

    @SerializedName("data")
    @Expose
    private T data;

    @SerializedName("code")
    @Expose
    private int code;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
