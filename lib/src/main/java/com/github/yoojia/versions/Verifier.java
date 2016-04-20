package com.github.yoojia.versions;

/**
 * 版本信息比较和校验
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 0.1
 */
public interface Verifier {

    /**
     * 比较远程版本与本地版本，是否允许更新
     * @param remoteVersion 远程版本信息
     * @param localVersion 本地版本信息
     * @return 是否可以更新
     */
    boolean accept(Version remoteVersion, Version localVersion);
}
