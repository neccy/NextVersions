package com.github.yoojia.versions.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.WindowManager;

import com.github.yoojia.versions.Notify;
import com.github.yoojia.versions.NotifyLevel;
import com.github.yoojia.versions.R;
import com.github.yoojia.versions.Version;

/**
 * 系统级对话框新版本提示
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class SystemDialogNotify implements Notify{

    private final int mLevel;
    private final Context mAppContext;

    public SystemDialogNotify(Context context) {
        this(context, NotifyLevel.SYSTEM_DIALOG);
    }

    public SystemDialogNotify(Context context, int level) {
        mAppContext = context.getApplicationContext();
        mLevel = level;
    }

    @Override
    public int onAcceptLevel() {
        return mLevel;
    }

    @Override
    public void onShow(final Version version) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mAppContext, R.style.Theme_System_Alert)
                .setTitle(version.name)
                .setMessage(version.note)
                .setCancelable(false)
                .setNegativeButton(R.string.name_later, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.name_upgrade, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //downloads.submit(context, version);
                        dialog.cancel();
                    }
                });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = builder.create();
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
            }
        });
    }

}
