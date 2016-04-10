package com.github.yoojia.anyversion;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Andy on 4/10/2016.
 */
public class Tools {

    public static Version getCurrentVersion(Context context) {
        String versionName = null;
        int versionCode = 0;
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versionCode = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return new Version(versionName, null, null, versionCode);
    }
}
