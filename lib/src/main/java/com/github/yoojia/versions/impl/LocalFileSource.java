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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 从指定本机文件夹中获取更新源信息。这个功能通常用于自动更新SD卡指定目录的APK文件。
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class LocalFileSource implements Source {

    private final String mDirPath;
    private final Context mContext;

    /**
     * 递归查找文件夹深度,默认为3级
     */
    private final AtomicInteger mMaxDirDeep = new AtomicInteger(3);

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

    /**
     * 设置查找文件夹深度，最大深度为5级
     * @param deep 文件夹深度
     */
    public void setFindDirDeep(int deep) {
        if (deep > 5) {
            deep = 5;
        }
        mMaxDirDeep.set(deep);
    }

    private Version findLatestApkVersion(List<File> apks){
        final PackageManager pm = mContext.getPackageManager();
        final String pkgName = mContext.getPackageName();
        final List<Version> versions = new ArrayList<>(apks.size());
        limitedFind(versions, apks, pm, pkgName);
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

    private void limitedFind(List<Version> output, List<File> files, PackageManager pm, String pkgName) {
        mMaxDirDeep.decrementAndGet();
        for (File file : files) {
            if (!file.isDirectory()) {
                final String uri = file.getAbsolutePath();
                final PackageInfo info = pm.getPackageArchiveInfo(uri, PackageManager.GET_ACTIVITIES);
                if (info != null && info.packageName.equals(pkgName)) {
                    output.add(new Version(info.versionCode, info.versionName,
                            pm.getApplicationLabel(info.applicationInfo).toString(),
                            uri));
                }
            }else{
                if (mMaxDirDeep.get() > 0) {
                    limitedFind(output, Arrays.asList(file.listFiles()), pm, pkgName);
                }
            }
        }
    }
}
