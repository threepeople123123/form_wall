package com.wj.form.wall.responseEunm;

/**
 * packageName com.wj.form_wall.responseEunm
 *
 * @author wj
 * @enumName ResponseEnum
 * @date 2026/2/27
 * @description 返回前端枚举
 */
public enum ResponseEnum {


    LOGIN_SUCCESS("登录成功",200);

    private final String message;
    private final int code;

    ResponseEnum(String message, int code){
        this.message = message;
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
