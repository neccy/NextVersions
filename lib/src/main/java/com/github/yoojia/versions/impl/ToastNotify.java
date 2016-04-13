package com.github.yoojia.versions.impl;

import com.github.yoojia.versions.Notify;
import com.github.yoojia.versions.NotifyLevel;
import com.github.yoojia.versions.Version;

/**
 * Toast显示新版本
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class ToastNotify implements Notify{

    @Override
    public int onAcceptLevel() {
        return NotifyLevel.TOAST;
    }

    @Override
    public void onShow(Version version) {

    }
}
