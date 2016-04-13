package com.github.yoojia.versions;

/**
 * 通知级别
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public interface NotifyLevel {

    /**
     * 系统级弹出对话框
     */
    int SYSTEM_DIALOG = 0;

    /**
     * 通知栏
     */
    int NOTIFICATION = 1;

    /**
     * Toast提示信息
     */
    int TOAST = 2;

    /**
     * 静默更新
     */
    int SLIENTLY = 3;

    /**
     * 默认新版本提示级别
     */
    int DEFAULT = SYSTEM_DIALOG;

}
