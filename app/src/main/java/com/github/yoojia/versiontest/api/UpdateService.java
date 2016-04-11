package com.github.yoojia.versiontest.api;

import com.github.yoojia.versiontest.api.entity.BaseEntity;
import com.github.yoojia.versiontest.api.entity.VersionInfo;
import com.github.yoojia.versiontest.api.param.CheckUpdateParam;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;


public interface UpdateService {

    @POST("http://bae.catuncle.cn/update.json")
    Observable<BaseEntity<VersionInfo>> update(@Body CheckUpdateParam param);

}

