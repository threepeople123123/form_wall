package com.future.campus.util;

import cn.hutool.crypto.SecureUtil;

/**
 * packageName com.wj.form_wall.util
 *
 * @author WJ
 * @className MySecurityUtil
 * @date 2026/2/27
 * @description 加密工具类
 */
public class MySecurityUtil {

    public static final String SALT = "wj";


    /**
     * 加密密码
     * @param password 密码
     * @return 返回加密后的密码
     */
    public static String md5Security(String password){
        return SecureUtil.md5(SALT + password);
    }
}
