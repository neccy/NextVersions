package com.github.yoojia.versions;

/**
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public final class NextContext {

    private final NextVersions mNextVersions;
    private final Notify mNotify;
    private final Version mLocalVersion;
    private final Download mDownload;

    public NextContext(NextVersions nextVersions, Notify notify, Version localVersion, Download download) {
        mNextVersions = nextVersions;
        mNotify = notify;
        mLocalVersion = localVersion;
        mDownload = download;
    }

    public NextVersions getNextVersions() {
        return mNextVersions;
    }

    public Notify getNotify() {
        return mNotify;
    }

    public Download getDownload() {
        return mDownload;
    }

    public Version getLocalVersion() {
        return mLocalVersion;
    }
}
