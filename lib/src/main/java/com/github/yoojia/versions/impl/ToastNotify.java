package com.github.yoojia.versions.impl;

import android.content.Context;
import android.widget.Toast;

import com.github.yoojia.versions.Notify;
import com.github.yoojia.versions.NotifyLevel;
import com.github.yoojia.versions.Version;

/**
 * Toast显示新版本
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class ToastNotify implements Notify{

    private final Context mContext;

    public ToastNotify(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public int onAcceptLevel() {
        return NotifyLevel.TOAST;
    }

    @Override
    public void onShow(Version version) {
        String tip = "发现新版本：" + version.name;
        Toast.makeText(mContext, tip, Toast.LENGTH_LONG).show();
    }
}
