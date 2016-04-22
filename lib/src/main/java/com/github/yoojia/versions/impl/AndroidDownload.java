package com.github.yoojia.versions.impl;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.github.yoojia.versions.Download;
import com.github.yoojia.versions.Version;

/**
 * 使用Android内置的DownloadManager下载APK文件
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class AndroidDownload implements Download {

    public static final String MIME_TYPE = "application/vnd.android.package-archive";

    private static final String DOWNLOAD_DIR = "Versions";

    private final DownloadManager mDownloadManager;

    private long mDownloadId;

    public AndroidDownload(Context context) {
        mDownloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    public void submit(Version version) {
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(version.url));
        request.setDestinationInExternalPublicDir(DOWNLOAD_DIR, getAPKName(version));
        request.setTitle(version.name);
        request.setDescription(version.note);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setMimeType(MIME_TYPE);
        mDownloadId = mDownloadManager.enqueue(request);
    }

    private String getAPKName(Version version) {
        final Uri uri = Uri.parse(version.url);
        final String name = uri.getLastPathSegment();
        if (TextUtils.isEmpty(name) || ! name.endsWith(".apk")) {
            return version.name + ".apk";
        }else{
            return name;
        }
    }

    public long getDownloadId() {
        return mDownloadId;
    }
}
