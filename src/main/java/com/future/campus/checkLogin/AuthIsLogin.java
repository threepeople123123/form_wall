package com.future.campus.checkLogin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * packageName com.wj.form.wall.annotation
 *
 * @author wangj
 * @enumName AuthIsLogin
 * @date 2026/3/2
 * @description 判断是否登录
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthIsLogin {
}
