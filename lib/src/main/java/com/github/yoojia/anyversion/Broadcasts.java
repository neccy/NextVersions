package com.github.yoojia.anyversion;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;


/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
public class Broadcasts {

    static final String BROADCAST_ACTION = AnyVersion.class.getName();
    static final String BROADCAST_DATA = "data";

    private Broadcasts() {}

    static void send(Application context, Version remoteVersion){
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(BROADCAST_DATA, remoteVersion);
        manager.sendBroadcast(intent);
    }

    public static void register(Context context, BroadcastReceiver receiver){
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(receiver, new IntentFilter(BROADCAST_ACTION));
    }

    public static void unregister(Context context, BroadcastReceiver receiver){
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.unregisterReceiver(receiver);
    }

    public static Version getData(Intent intent){
        return intent.getParcelableExtra(BROADCAST_DATA);
    }
}
