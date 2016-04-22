package com.github.yoojia.versions.impl;

import com.github.yoojia.versions.Verifier;
import com.github.yoojia.versions.Version;

/**
 * 校验远程版本与本地版本是否可以更新
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 0.1
 */
public class SimpleVerifier implements Verifier {

    @Override
    public boolean accept(Version remoteVersion, Version localVersion) {
        // 相同Channel和比本地版本要新的情况下可以更新
        return remoteVersion.isSameChannel(localVersion) &&
                remoteVersion.isNewerThen(localVersion);
    }

}
