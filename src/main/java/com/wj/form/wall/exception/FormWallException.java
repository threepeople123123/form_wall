package com.wj.form.wall.exception;

import lombok.Data;

/**
 * packageName com.wj.form_wall.exception
 *
 * @author wj
 * @className FormWallException
 * @date 2026/2/27
 * @description 异常抛出统一类
 */
@Data
public class FormWallException extends Throwable{

    private String meessage;

    private int code = 500;

    public FormWallException(String meessage){
        super(meessage);
        this.meessage = meessage;
    }

    public FormWallException(String meessage,int code){
        super(meessage);
        this.meessage = meessage;
        this.code = code;
    }
}
