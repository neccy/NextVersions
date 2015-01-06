package com.github.yoojia.versiontest;

import android.app.Application;

import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.Parser;
import com.github.yoojia.anyversion.Version;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-06
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println(">>>>> On application create ....");

        AnyVersion.init(this, new Parser() {
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
    }
}
