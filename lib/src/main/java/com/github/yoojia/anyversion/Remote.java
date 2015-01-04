package com.github.yoojia.anyversion;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
class Remote implements Runnable{

    private final String URL;
    private final Parser parser;
    private final Callback callback;

    public Remote(String url, Parser parser, Callback callback) {
        URL = url;
        this.parser = parser;
        this.callback = callback;
    }

    @Override
    public void run() {
        String response = "{'name':'TEST APK', 'code': 99, 'note':'@TomBennett and anyone else who might still be interested, The situation has changed somewhat over the last two years this question was originally asked. APKs can now be installed remotely through Google Play. The user still gets a set of permissions to accept in order to install an app, but that prompt can now be done over the Google Play web site (so it should be much easier to automate for your test suite with tools like Selenium). And also, now Google Play supports limited Beta releases, thus allowing you to automate the testing process before your app gets into production', 'url':'http://apk.r1.market.hiapk.com/data/upload/2015/01_01/20/com.sohu.inputmethod.sogou_204705.apk'}";
        Version version = null;
        try{
            version = parser.onParse(response);
        }catch (Exception ex){ /* Nothing */ }
        callback.onVersion(version);
    }
}
