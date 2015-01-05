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

    Application context;
    Parser parser;

    private Future<?> workingTask;
    private Version currentVersion;
    private Callback callback;
    private String url;
    private ExecutorService threads = Executors.newSingleThreadExecutor();
    private Handler mainHandler;
    private RemoteRequest remoteRequest;

    public static void init(final Application context, Parser parser){
        Enforce.mainUIThread();
        if (ANY_VERSION.context != null){
            throw new IllegalStateException("Duplicate call init !");
        }
        if (context == null) {
            throw new NullPointerException("Application Context CANNOT be null !");
        }
        if (parser == null) {
            throw new NullPointerException("Parser CANNOT be null !");
        }
        ANY_VERSION.context = context;
        ANY_VERSION.parser = parser;
        ANY_VERSION.mainHandler = new Handler(Looper.getMainLooper()){
            @Override public void handleMessage(Message msg) {
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
        Enforce.init();
        if (callback == null){
            throw new NullPointerException("Callback CANNOT be null !");
        }
        this.callback = callback;
    }

    public void setURL(String url){
        Enforce.init();
        if (TextUtils.isEmpty(url)){
            throw new NullPointerException("URL CANNOT be null or empty !");
        }
        this.url = url;
    }

    public void setRemoteRequest(RemoteRequest remoteRequest){
        Enforce.init();
        if (remoteRequest == null){
            throw new NullPointerException("remoteRequest CANNOT be null !");
        }
        this.remoteRequest = remoteRequest;
    }

    public void check(final String url, final RemoteRequest remote, final NotifyStyle style){
        Enforce.init();
        if (NotifyStyle.Callback.equals(style) && callback == null){
            throw new NullPointerException("If reply by callback, callback CANNOT be null ! " +
                    "Call 'setCallback(...) to setup !'");
        }
        final Callback core = new Callback() {
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
        };
        remote.setOptions(url, parser, core);
        workingTask = threads.submit(remote);
    }

    public void check(NotifyStyle style){
        createRemoteRequestIfNeed();
        check(this.url, this.remoteRequest, style);
    }

    public void cancelCheck(){
        Enforce.init();
        if (workingTask != null && !workingTask.isDone()){
            workingTask.cancel(true);
        }
    }

    public void destroy(){
        Enforce.init();
        cancelCheck();
        threads.shutdown();
    }

    private void createRemoteRequestIfNeed(){
        if (this.remoteRequest == null){
            this.remoteRequest = new SimpleRemoteRequest();
        }
    }

}
