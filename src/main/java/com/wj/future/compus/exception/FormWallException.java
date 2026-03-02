package com.wj.future.compus.exception;

/**
 * packageName com.wj.form_wall.exception
 *
 * @author wj
 * @className FormWallException
 * @date 2026/2/27
 * @description 异常抛出统一类
 */
public class FormWallException extends Exception{

    private String message;

    private int code = 500;

    public FormWallException(String meessage){
        super(meessage);
        this.message = meessage;
    }

    public FormWallException(String message,int code){
        super(message);
        this.message = message;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
