package com.wj.future.campus.result;

import lombok.Data;

/**
 * packageName com.wj.form_wall.result
 *
 * @author wj
 * @className R
 * @date 2026/2/27
 * @description 返回前端消息体
 */
@Data
public class R<T> {

    private T data;

    private int code;

    private String message;


    public static <T> R<T> ok(){
        R<T> r = new R<>();
        r.setData(null);
        r.setCode(200);
        r.setMessage("成功");
        return r;
    }

    public static <T> R<T> ok(String message){
        R<T> r = new R<>();
        r.setData(null);
        r.setCode(200);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> ok(T data){
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(200);
        r.setMessage("成功");
        return r;
    }

    public static <T> R<T> ok(T data,int code){
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(code);
        r.setMessage("成功");
        return r;
    }

    public static <T> R<T> ok(String message,int code){
        R<T> r = new R<>();
        r.setData(null);
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> ok(T data,String message){
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(200);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> ok(T data,int code,String message){
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> failure(){
        R<T> r = new R<>();
        r.setData(null);
        r.setCode(500);
        r.setMessage("失败");
        return r;
    }

    public static <T> R<T> failure(String message){
        R<T> r = new R<>();
        r.setData(null);
        r.setCode(500);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> failure(T data,int code){
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(code);
        r.setMessage("失败");
        return r;
    }

    public static <T> R<T> failure(T data,String message){
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(500);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> failure(T data,String message,int code){
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(code);
        r.setMessage(message);
        return r;
    }



}
