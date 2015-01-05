package com.github.yoojia.anyversion;

import android.app.Application;
import android.content.Context;
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
 * AnyVersion - 自动更新 APK
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
    private Handler mainHandler;
    private RemoteRequest remoteRequest;
    private final ExecutorService threads = Executors.newSingleThreadExecutor();
    private final Installations installations = new Installations();
    private final Downloads downloads = new Downloads();

    /**
     * 初始化 AnyVersion
     * @param context 必须是 Application
     * @param parser 服务端响应数据解析器
     */
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
                new VersionDialog(context, version, ANY_VERSION.downloads).show();
            }
        };
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            ANY_VERSION.currentVersion = new Version(pi.versionName, null, null, pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            ANY_VERSION.currentVersion = new Version(null, null, null, 0);
        }
        ANY_VERSION.installations.register(context);
    }

    /**
     * 设置发现新版本时的回调接口。当 check(NotifyStyle.Callback) 时，此接口参数生效。
     */
    public void setCallback(Callback callback){
        Enforce.init();
        if (callback == null){
            throw new NullPointerException("Callback CANNOT be null !");
        }
        this.callback = callback;
    }

    /**
     * 设置检测远程版本的 URL。在使用内置 RemoteRequest 时，URL 是必须的。
     */
    public void setURL(String url){
        Enforce.init();
        checkRequiredURL(url);
        this.url = url;
    }

    /**
     * 设置自定义检测远程版本数据的接口
     */
    public void setRemoteRequest(RemoteRequest remoteRequest){
        Enforce.init();
        if (remoteRequest == null){
            throw new NullPointerException("remoteRequest CANNOT be null !");
        }
        this.remoteRequest = remoteRequest;
    }

    /**
     * 检测新版本，并指定发现新版本的处理方式
     */
    public void check(NotifyStyle style){
        // 使用内置请求时，URL 地址是必须的。
        checkRequiredURL(this.url);
        createRemoteRequestIfNeed();
        check(this.url, this.remoteRequest, style);
    }

    /**
     * 按指定的 URL，检测新版本，并指定发现新版本的处理方式
     */
    public void check(String url, NotifyStyle style){
        createRemoteRequestIfNeed();
        check(url, this.remoteRequest, style);
    }

    private void check(final String url, final RemoteRequest remote, final NotifyStyle style){
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

    /**
     * 取消当前正在检测的工作线程
     */
    public void cancelCheck(){
        Enforce.init();
        if (workingTask != null && !workingTask.isDone()){
            workingTask.cancel(true);
        }
    }

    /**
     * 销毁 AnyVersion 服务
     */
    public void destroy(){
        Enforce.init();
        cancelCheck();
        threads.shutdownNow();
        downloads.destroy(context);
        installations.unregister(context);
        context = null; // !!! required
    }

    private void createRemoteRequestIfNeed(){
        if (this.remoteRequest == null){
            this.remoteRequest = new SimpleRemoteRequest();
        }
    }

    private void checkRequiredURL(String url){
        if (TextUtils.isEmpty(url)){
            throw new NullPointerException("URL CANNOT be null or empty !");
        }
    }

}
