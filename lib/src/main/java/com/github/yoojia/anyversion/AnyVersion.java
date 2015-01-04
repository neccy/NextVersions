package com.github.yoojia.anyversion;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
public class AnyVersion {

    private static final AnyVersion ANY_VERSION = new AnyVersion();

    private AnyVersion() {}

    public static AnyVersion getInstance(){
        return ANY_VERSION;
    }

    private Future<?> workingTask;
    private Version currentVersion;
    private Application context;
    private String URL;
    private Callback callback;
    private Parser parser;
    private ExecutorService threads = Executors.newSingleThreadExecutor();
    private Handler mainHandler;

    public static void init(final Application context, String url, Parser parser){
        if (ANY_VERSION.context != null){
            throw new IllegalStateException("Duplicate init !");
        }
        if (context == null) {
            throw new NullPointerException("Application Context CANNOT be null !");
        }
        if (parser == null) {
            throw new NullPointerException("Parser CANNOT be null !");
        }
        if (TextUtils.isEmpty(url)){
            throw new NullPointerException("URL CANNOT be null or empty !");
        }
        ANY_VERSION.context = context;
        ANY_VERSION.URL = url;
        ANY_VERSION.parser = parser;
        ANY_VERSION.mainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Version version = (Version) msg.obj;
                new VersionDialog(context, version).show();
            }
        };
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            ANY_VERSION.currentVersion = new Version(pi.versionName, null, null, pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            ANY_VERSION.currentVersion = new Version(null, null, null, 0);
        }
    }

    public void setCallback(Callback callback){
        if (callback == null){
            throw new NullPointerException("Callback CANNOT be null !");
        }
        this.callback = callback;
    }

    public void startCheck(final NotifyStyle style){
        if (NotifyStyle.Callback.equals(style) && callback == null){
            throw new NullPointerException("If reply by callback, callback CANNOT be null ! " +
                    "Call 'setCallback(...) to setup !'");
        }
        workingTask = threads.submit(new Remote(URL, parser, new Callback() {
            @Override public void onVersion(Version remoteVersion) {
                // 检查是否为新版本
                if (remoteVersion == null) return;
                if (currentVersion.code >= remoteVersion.code) return;
                switch (style){
                    case Callback:
                        callback.onVersion(remoteVersion);
                        break;
                    case Broadcast:
                        Broadcasts.send(context, remoteVersion);
                        break;
                    case Dialog:
                        final Message msg = Message.obtain(ANY_VERSION.mainHandler, 0, remoteVersion);
                        msg.sendToTarget();
                        break;
                }
            }
        }));
    }

    public void cancelCheck(){
        if (workingTask != null && !workingTask.isDone()){
            workingTask.cancel(true);
        }
    }

    public void destroy(){
        cancelCheck();
        threads.shutdown();
    }

}
