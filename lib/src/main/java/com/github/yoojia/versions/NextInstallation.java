package com.github.yoojia.versions;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;

/**
 *
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class NextInstallation extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        final long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        // 下载完成，自动安装
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        DownloadManager download = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = download.query(query);
        try{
            if (cursor.moveToFirst()) {
                final int index = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                final String fileName = cursor.getString(index);
                final Intent install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(Uri.fromFile(new File(fileName)), NextVersions.APK_MIME_TYPE);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(install);
            }
        }finally {
            cursor.close();
        }
    }

    public void register(Context context) {
        context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }


}
