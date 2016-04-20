package com.github.yoojia.versions;

import android.text.TextUtils;

/**
 * 应用版本信息
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class Version {

    public static final Version NONE = new Version(0, "none", null, null);

    /**
     * 版本代码
     */
    public final int code;

    /**
     * 版本名称
     */
    public final String name;

    /**
     * 更新说明
     */
    public final String note;

    /**
     * 新版本下载地址
     */
    public final String url;

    /**
     * 新版本提示级别
     */
    public final int level;

    /**
     * 渠道号
     */
    public final String channel;

    public Version(int code, String name, String note, String url, int level, String channel) {
        this.code = code;
        this.name = name;
        this.note = note;
        this.url = url;
        this.level = level;
        this.channel = channel;
    }

    public Version(int code, String name, String note, String url) {
        this(code, name, note, url, NotifyLevel.SYSTEM_DIALOG, null);
    }

    /**
     * 当前版本是否比指定版本要新
     * @param version 指定版本信息
     * @return 是否比指定版本要新
     */
    public boolean isNewerThen(Version version) {
        return code > version.code;
    }

    /**
     * 是否为同一渠道号的版本
     * @param version 版本数据
     * @return 是否同一渠道
     */
    public boolean isSameChannel(Version version) {
        if (TextUtils.isEmpty(this.channel)) {
            return true;
        }else{
            return this.channel.equals(version.channel);
        }
    }

    @Override
    public String toString() {
        return "{" +
                "channel='" + channel + '\'' +
                ", code=" + code +
                ", name='" + name + '\'' +
                ", note='" + note + '\'' +
                ", url='" + url + '\'' +
                ", level=" + level +
                '}';
    }
}
