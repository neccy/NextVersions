package com.github.yoojia.versions.impl;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.github.yoojia.versions.Source;
import com.github.yoojia.versions.Version;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 从指定更新源URL中获取JSON响应，获取版本信息。
 * 接受返回JSON响应的格式为：
 *
 * Status: 200 OK
 * {
 *     "code": 0,
 *     "name": "2.0-beta",
 *     "note": "更新说明",
 *     "url": "http://domain.com/files/someapk.apk",
 *     "level": 1000,
 *     "channel": "stable"
 * }
 *
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public class RemoteJSONSource implements Source {

    private static final String TAG = RemoteJSONSource.class.getSimpleName();

    private static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000;
    private static final int DEFAULT_HTTP_READ_TIMEOUT = 10 * 1000;
    private static final int MAX_REDIRECT_COUNT = 3;
    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    private final String mURL;

    public RemoteJSONSource(String url) {
        mURL = url;
    }

    @Override
    public Version versionFromSource() {
        try{
            final InputStream is = getStreamFromNetwork(mURL);
            final String responseText = toStringBuffer(is).toString();
            if (TextUtils.isEmpty(responseText)) {
                throw new IOException("Response string NOT JSON object text: " + responseText);
            }
            final JSONTokener tokener = new JSONTokener(responseText);
            final JSONObject json = (JSONObject) tokener.nextValue();
            return new Version(
                    getInt(json, "code"),
                    getString(json, "name"),
                    getString(json, "note"),
                    getString(json, "url"),
                    getInt(json, "level"),
                    getString(json, "channel"));
        }catch (Throwable errors) {
            Log.e(TAG, "When network fetch JSON response", errors);
            return Version.NONE;
        }
    }

    private static String getString(JSONObject json, String field) throws JSONException {
        return json.has(field) ? json.getString(field) : "";
    }

    private static int getInt(JSONObject json, String field) throws JSONException {
        return json.has(field) ? json.getInt(field) : 0;
    }

    private StringBuilder toStringBuffer(InputStream is) throws IOException {
        if( null == is) return null;
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder buffer = new StringBuilder();
        String line ;
        while ((line = in.readLine()) != null){
            buffer.append(line).append("\n");
        }
        is.close();
        return buffer;
    }

    private HttpURLConnection createConnection(String url) throws IOException {
        final String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
        final HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
        conn.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_HTTP_READ_TIMEOUT);
        return conn;
    }

    private InputStream getStreamFromNetwork(String url) throws IOException {
        HttpURLConnection conn = createConnection(url);
        int redirectCount = 0;
        final int httpCode = conn.getResponseCode();
        while (httpCode / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
            conn = createConnection(conn.getHeaderField("Location"));
            redirectCount++;
        }
        final InputStream stream = conn.getInputStream();
        if (httpCode != 200) {
            closeSilently(stream);
            throw new IOException("URL request failed with response code " + httpCode);
        }
        return stream;
    }

    private void closeSilently(Closeable closeable) throws IOException{
        if (closeable != null) {
            closeable.close();
        }
    }

}
