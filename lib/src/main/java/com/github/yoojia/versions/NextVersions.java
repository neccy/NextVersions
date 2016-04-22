package com.github.yoojia.versions;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.github.yoojia.versions.impl.AndroidDownload;
import com.github.yoojia.versions.impl.SimpleVerifier;
import com.github.yoojia.versions.impl.DefaultNotify;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class NextVersions {

    public static final String APK_MIME_TYPE = "application/vnd.android.package-archive";
    public static final String APK_SUFFIX = ".apk";

    public static final String TAG = NextVersions.class.getSimpleName();

    private final AtomicBoolean mLogEnabled = new AtomicBoolean(true);
    private final Notifications mNotifications = new Notifications();
    private final List<Source> mAllSources = new CopyOnWriteArrayList<>();
    private final NextInstallation mInstallation = new NextInstallation();

    private final SourceFetcher mFetcher;

    private Verifier mVerifier = new SimpleVerifier();
    private Download mDownload;

    public NextVersions(final Activity context) {
        final Application appContext = (Application) context.getApplicationContext();
        mDownload = new AndroidDownload(appContext);
        final Version localVersion = getAppVersion(appContext);
        mFetcher = new SourceFetcher(new OnVersionHandler(localVersion));
        // 默认新版本提示
        putNotify(new DefaultNotify(appContext, NotifyLevel.DEFAULT));
        // 自动监听下载完成广播
        appContext.registerActivityLifecycleCallbacks(new LifeCycleInject(){
            @Override
            public void onActivityResumed(Activity activity) {
                super.onActivityResumed(activity);
                if (context == activity) {
                    mInstallation.register(activity);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                super.onActivityPaused(activity);
                if (context == activity) {
                    mInstallation.unregister(activity);
                }
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                super.onActivityDestroyed(activity);
                if (context == activity) {
                    appContext.unregisterActivityLifecycleCallbacks(this);
                }
            }
        });
        if (mLogEnabled.get()) {
            Log.d(TAG, "--> App version: " + localVersion);
        }
    }

    /**
     * 增加更新源接口
     * @param source 更新源
     */
    public void addSource(Source source) {
        mAllSources.add(source);
    }

    /**
     * 设置下载器接口
     * @param download 下载器
     */
    public void setDownload(Download download) {
        if (download == null) {
            throw new NullPointerException();
        }
        mDownload = download;
    }

    /**
     * 设置版本校验接口
     * @param verifier 版本校验接口
     */
    public void setVerifier(Verifier verifier) {
        if (verifier == null) {
            throw new NullPointerException();
        }
        mVerifier = verifier;
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
        mFetcher.submit(mAllSources);
    }

    public void setLogEnabled(boolean enabled) {
        mLogEnabled.set(enabled);
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
            return new Version(Integer.MIN_VALUE, "PKG-NOT-FOUND", null, null);
        }
    }

    private class OnVersionHandler implements SourceFetcher.OnVersionHandler {

        private final Version mLocalVersion;

        private OnVersionHandler(Version localVersion) {
            mLocalVersion = localVersion;
        }

        @Override
        public boolean onVersion(Version remoteVersion) {
            if (mLogEnabled.get()) {
                Log.d(TAG, "--> Remote version: " + remoteVersion);
            }
            if (mVerifier.accept(remoteVersion, mLocalVersion)) {
                Notify notify = mNotifications.getPresent(remoteVersion.level);
                if (notify == null) {
                    notify = mNotifications.getPresent(NotifyLevel.DEFAULT);
                }
                final NextContext ctx = new NextContext(NextVersions.this, notify, mLocalVersion, mDownload);
                notify.onShow(ctx, remoteVersion);
                return true;
            }else{
                return false;
            }
        }
    }

}
