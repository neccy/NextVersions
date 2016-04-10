package com.github.yoojia.anyversion;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 * AnyVersion - 自动更新 APK
 */
public class AnyVersion {

    private static final String TAG = "AnyVersion";

    private static final Lock LOCK = new ReentrantLock();

    private static AnyVersion ANY_VERSION = null;

    Application context;
    private Callback callback;
    private final Version currentVersion;
    private final Handler mainHandler;
    private final Installations installations;
    private final Downloads downloads;

    public static AnyVersion getInstance(){
        try{
            LOCK.lock();
            if (ANY_VERSION == null) {
                throw new IllegalStateException("AnyVersion NOT init !");
            }
            return ANY_VERSION;
        }finally {
            LOCK.unlock();
        }
    }

    private AnyVersion(Application context) {
        this.context = context;
        this.installations  = new Installations();
        this.downloads = new Downloads();
        this.mainHandler = new Handler(Looper.getMainLooper()){
            @Override public void handleMessage(Message msg) {
                Version version = (Version) msg.obj;
                new VersionDialog(AnyVersion.this.context, version, AnyVersion.this.downloads).show();
            }
        };
        currentVersion = Tools.getCurrentVersion(context);
    }

    /**
     * 初始化 AnyVersion。
     * @param context 必须是 Application
     */
    public static void init(Application context){
        Preconditions.requiredMainUIThread();
        try{
            LOCK.lock();
            if (ANY_VERSION != null) {
                Log.e(TAG, "AnyVersion recommend init on YOUR-Application.onCreate(...) .");
                return;
            }
        }finally {
            LOCK.unlock();
        }
        if (context == null) {
            throw new NullPointerException("Application Context CANNOT be null !");
        }
        ANY_VERSION = new AnyVersion(context);
        ANY_VERSION.installations.register(context);
    }

    /**
     * 注册接收新版本通知的 Receiver。
     */
    public static void registerReceiver(Context context, VersionReceiver receiver){
        Broadcasts.register(context, receiver);
    }

    /**
     * 反注册接收新版本通知的 Receiver
     */
    public static void unregisterReceiver(Context context, VersionReceiver receiver){
        Broadcasts.unregister(context, receiver);
    }

    /**
     * 设置发现新版本时的回调接口。当 check(NotifyStyle.Callback) 时，此接口参数生效。
     */
    public void setCallback(Callback callback){
        Preconditions.requireInited();
        if (callback == null){
            throw new NullPointerException("Callback CANNOT be null !");
        }
        this.callback = callback;
    }

    /**
     * 检测新版本，并指定发现新版本的处理方式
     */
    public void check(Version remoteVersion, final NotifyStyle style) {
        Preconditions.requireInited();

        if (NotifyStyle.Callback.equals(style) && callback == null) {
            throw new NullPointerException("If reply by callback, callback CANNOT be null ! " +
                    "Call 'setCallback(...) to setup !'");
        }

        // 检查是否为新版本
        if (remoteVersion == null) return;
        if (currentVersion.code >= remoteVersion.code) return;

        switch (style) {
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
}
