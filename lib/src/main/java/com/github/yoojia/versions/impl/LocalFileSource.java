package com.github.yoojia.versions.impl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.github.yoojia.versions.Source;
import com.github.yoojia.versions.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 从指定本机文件夹中获取更新源信息。
 * 这个功能通常用于自动更新SD卡指定目录的APK文件。
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class LocalFileSource implements Source {

    private final String mDirPath;
    private final Context mContext;

    public LocalFileSource(Context context, String mDirPath) {
        this.mDirPath = mDirPath;
        mContext = context.getApplicationContext();
    }

    @Override
    public Version versionFromSource() {
        final File dir = new File(mDirPath);
        if (dir.isDirectory()) {
            if (dir.exists()) {
                // Find all .apk files
                final File[] files = dir.listFiles();
                final List<File> apks = new ArrayList<>(files.length);
                for (File file : files) {
                    final String name = file.getName();
                    if (name.endsWith(".apk") || name.endsWith(".APK")) {
                        apks.add(file);
                    }
                }
                return findLatestApkVersion(apks);
            }else{
                return Version.NONE;
            }
        }else{
            throw new IllegalArgumentException("Path must be a directory: " + mDirPath);
        }
    }

    private Version findLatestApkVersion(List<File> apks){
        final PackageManager pm = mContext.getPackageManager();
        final String pkgName = mContext.getPackageName();
        final List<Version> versions = new ArrayList<>(apks.size());
        for (File file : apks) {
            final String uri = file.getAbsolutePath();
            final PackageInfo info = pm.getPackageArchiveInfo(uri, PackageManager.GET_ACTIVITIES);
            if (info != null && info.packageName.equals(pkgName)) {
                versions.add(new Version(info.versionCode, info.versionName,
                        pm.getApplicationLabel(info.applicationInfo).toString(),
                        uri));
            }
        }
        if (versions.isEmpty()) {
            return Version.NONE;
        }
        final Version[] sorted = versions.toArray(new Version[apks.size()]);
        Arrays.sort(versions.toArray(sorted), new Comparator<Version>() {
            @Override
            public int compare(Version lhs, Version rhs) {
                return lhs.code - rhs.code;
            }
        });
        return sorted[0];
    }

}
