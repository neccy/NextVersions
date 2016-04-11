package com.github.yoojia.versiontest;


import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Rx工具类
 */
public class RxUtils {


    /**
     * 显示并隐藏loading
     */
    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> showLoading(final ILoading loading) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .doOnSubscribe(new Action0() {
                            @Override
                            public void call() {
                                loading.showLoading();
                            }
                        })
                        .doOnTerminate(new Action0() {
                            @Override
                            public void call() {
                                loading.hideLoading();
                            }
                        }).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

}
