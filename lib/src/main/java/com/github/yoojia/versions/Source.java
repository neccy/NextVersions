package com.github.yoojia.versions;

/**
 * 更新源
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
public interface Source {

    /**
     * 返回更新源的版本信息
     * @return 更新源对象
     */
    Version versionFromSource();
}
