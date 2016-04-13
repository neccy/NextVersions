package com.github.yoojia.versions;

/**
 * 新版本通知
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public interface Notify {

    /**
     * 通知实现可处理的通知级别
     * @return 通知级别
     */
    int onAcceptLevel();

    /**
     * 将新版本通知展示给用户
     * @param version 新版本
     */
    void onShow(Version version);
}
