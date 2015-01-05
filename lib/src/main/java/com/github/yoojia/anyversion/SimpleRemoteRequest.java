package com.github.yoojia.anyversion;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-05
 */
class SimpleRemoteRequest extends RemoteRequest {

    @Override
    public String request(String url) {
        return "{'name':'v2.0.2-alpha', 'code': 28, 'note': 'New version', 'url': 'http://abc.com/apk.apk'}";
    }
}
