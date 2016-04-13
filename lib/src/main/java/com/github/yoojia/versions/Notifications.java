package com.github.yoojia.versions;

import android.util.SparseArray;

/**
 * 新版本展示方式管理器
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
class Notifications {

    private final SparseArray<Notify> mNotifyArray = new SparseArray<>();

    public void putNotify(Notify notify) {
        mNotifyArray.put(notify.onAcceptLevel(), notify);
    }

    public Notify getPresent(int level) {
        return mNotifyArray.get(level);
    }
}
