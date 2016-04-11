package com.github.yoojia.versiontest;

import android.content.Context;
import android.widget.Toast;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Andy on 4/9/2016.
 */
public class Tools {
    private static Context context;

    public static void init(Context context){
        Tools.context = context;
    }

    public static void toast(String tip) {
        Observable.just(tip)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
