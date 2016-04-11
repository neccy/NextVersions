package com.github.yoojia.versiontest.model;

import com.github.yoojia.anyversion.Version;
import com.github.yoojia.versiontest.api.UpdateService;
import com.github.yoojia.versiontest.api.entity.BaseEntity;
import com.github.yoojia.versiontest.api.entity.VersionInfo;
import com.github.yoojia.versiontest.api.param.CheckUpdateParam;
import com.github.yoojia.versiontest.api.utils.ServiceGenerator;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Andy on 4/7/2016.
 */
public class UpdateModel {


    private static UpdateModel instance;
    private UpdateService service;

    synchronized public static UpdateModel getInstance() {
        if (instance == null) {
            synchronized (UpdateModel.class) {
                if (instance == null) {
                    instance = new UpdateModel();
                }
            }
        }
        return instance;
    }

    private UpdateModel() {
        this.service = ServiceGenerator.createService(UpdateService.class);
    }

    public Observable<Version> update(CheckUpdateParam param, boolean testFlag) {
        Observable<BaseEntity<VersionInfo>> observable;

        if (testFlag) {
            observable = Observable.create(new Observable.OnSubscribe<BaseEntity<VersionInfo>>() {
                @Override
                public void call(Subscriber<? super BaseEntity<VersionInfo>> subscriber) {
                    BaseEntity<VersionInfo> entity = new BaseEntity<>();
                    entity.setCode(1);
                    sleep();
                    subscriber.onNext(entity);
                    subscriber.onCompleted();
                }
            }).subscribeOn(Schedulers.io());
        } else {
            observable = service.update(param)
                    .subscribeOn(Schedulers.io());
        }
        return observable.map(new Func1<BaseEntity<VersionInfo>, Version>() {
            @Override
            public Version call(BaseEntity<VersionInfo> entity) {
                if (entity.getCode() == 0 && entity.getData() != null) {
                    return entity.getData().createVersionObj();
                }
                return null;
            }
        });

    }

    /**
     * waste time for demo
     */
    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
