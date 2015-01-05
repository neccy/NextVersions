package com.github.yoojia.versiontest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.AutoInstall;
import com.github.yoojia.anyversion.Broadcasts;
import com.github.yoojia.anyversion.Callback;
import com.github.yoojia.anyversion.Downloads;
import com.github.yoojia.anyversion.NotifyStyle;
import com.github.yoojia.anyversion.Parser;
import com.github.yoojia.anyversion.RemoteRequest;
import com.github.yoojia.anyversion.Version;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
public class MainActivity extends Activity{

    class TestReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Version version = Broadcasts.getData(intent);
            System.out.println(">> Broadcast === \n" + version);
        }
    }

    private TestReceiver testReceiver = new TestReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        AnyVersion.init(getApplication(), new Parser() {
            @Override
            public Version onParse(String response) {
                final JSONTokener tokener = new JSONTokener(response);
                try {
                    JSONObject json = (JSONObject) tokener.nextValue();
                    return new Version(
                            json.getString("name"),
                            json.getString("note"),
                            json.getString("url"),
                            json.getInt("code")
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        AnyVersion version = AnyVersion.getInstance();
//        version.setRemoteRequest(new RemoteRequest() {
//            @Override
//            public String request(String url) {
//                return null;
//            }
//        });


        version.setCallback(new Callback() {
            @Override
            public void onVersion(Version version) {
                System.out.println(">> Callback == \n" + version);
            }
        });

        Button checkBroadcast = (Button) findViewById(R.id.broadcast);
        checkBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnyVersion version = AnyVersion.getInstance();
                version.check(NotifyStyle.Broadcast);
            }
        });

        Button checkCallback = (Button) findViewById(R.id.callback);
        checkCallback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnyVersion version = AnyVersion.getInstance();
                version.check(NotifyStyle.Callback);
            }
        });

        Button checkDialog = (Button) findViewById(R.id.dialog);
        checkDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnyVersion version = AnyVersion.getInstance();
                version.check(NotifyStyle.Dialog);
            }
        });

        AutoInstall.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Broadcasts.register(this, testReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Broadcasts.unregister(this, testReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AnyVersion version = AnyVersion.getInstance();
        version.destroy();
        Downloads.destroy(this);
        AutoInstall.destroy(this);
    }
}
