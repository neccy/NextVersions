package com.github.yoojia.anyversion;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-05
 */
public abstract class RemoteRequest implements Runnable {

    private String url;
    private Parser parser;
    private Callback callback;

    final void setOptions(String url, Parser parser, Callback callback){
        this.url =  url;
        this.parser = parser;
        this.callback = callback;
    }

    @Override
    final public void run() {
        String response = request(this.url);
        Version version = null;
        try{
            version = parser.onParse(response);
        }catch (Exception ex){ /* Nothing */ }
        callback.onVersion(version);
    }

    public abstract String request(String url);

}
