package com.github.yoojia.versions;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.github.yoojia.versions.impl.SystemDialogNotify;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class NextVersions {

    public static final String TAG = NextVersions.class.getSimpleName();

    private final Notifications mNotifications = new Notifications();
    private final List<Source> mSources = new CopyOnWriteArrayList<>();
    private final SourceFetcher mFetcher;

    public NextVersions(Context context) {
        final Version appVersion = getAppVersion(context.getApplicationContext());
        System.out.println("--> App version: " + appVersion);
        mFetcher = new SourceFetcher(new SourceFetcher.OnVersionHandler() {
            @Override
            public boolean onVersion(Version remoteVersion) {
                System.out.println("--> Remote version: " + remoteVersion);
                // 发现新版本
                if (remoteVersion.isNewerThen(appVersion)) {
                    Notify notify = mNotifications.getPresent(remoteVersion.level);
                    if (notify == null) {
                        notify = mNotifications.getPresent(NotifyLevel.DEFAULT);
                    }
                    notify.onShow(remoteVersion);
                    return true;
                }else{
                    return false;
                }
            }
        });
        // 默认新版本提示
        putNotify(new SystemDialogNotify(context, NotifyLevel.DEFAULT));
    }

    /**
     * 增加更新源接口
     * @param source 更新源
     */
    public void addSource(Source source) {
        mSources.add(source);
    }

    /**
     * 设置下载器接口
     * @param downloader 下载器
     */
    public void setDownloader(Downloader downloader) {

    }

    /**
     * 增加新版本通知处理接口
     * @param notify 通知处理接口
     */
    public void putNotify(Notify notify) {
        mNotifications.putNotify(notify);
    }

    /**
     * 检查更新
     */
    public void checkUpdate(){
        mFetcher.submit(mSources);
    }

    private Version getAppVersion(Context context) {
        try {
            final PackageManager pm = context.getPackageManager();
            final String packageName = context.getPackageName();
            final PackageInfo info = pm.getPackageInfo(packageName, 0);
            final String name = info.versionName;
            final int code = info.versionCode;
            final Bundle metaData = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData;
            String channel = null;
            if (! metaData.containsKey("versions-channel")) {
                Log.e(TAG, "MetaData(name: versions-channel) tag is recommend to set in <application> node of AndroidManifest.xml, " +
                        "e.g: <meta-data android:name=\"versions-channel\" android:value=\"github-release\"/>");
            }else{
                channel = metaData.getString("versions-channel");
            }
            return new Version(code, name, packageName, "installed-apk", NotifyLevel.DEFAULT, channel);
        } catch (Exception e) {
            Log.e(TAG, "Package not found! Pkg:" + context.getPackageName(), e);
            return new Version(Integer.MIN_VALUE, "PKG-NOTFOUND", null, null);
        }
    }

}
