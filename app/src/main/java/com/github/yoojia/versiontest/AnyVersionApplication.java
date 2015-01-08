package com.github.yoojia.versiontest;

import android.app.Application;
import android.util.Log;

import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.Version;
import com.github.yoojia.anyversion.VersionParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-06
 */
public class AnyVersionApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("App",">>>>>>>>>>> AnyVersion App init ...");
        AnyVersion.init(this, new VersionParser() {
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
