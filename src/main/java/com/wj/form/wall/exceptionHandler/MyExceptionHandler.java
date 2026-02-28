package com.wj.form.wall.exceptionHandler;

import com.wj.form.wall.exception.FormWallException;
import com.wj.form.wall.result.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * packageName com.wj.form.wall.exceptionHandler
 *
 * @author wj
 * @className MyExceptionHandler
 * @date 2026/2/28
 * @description 异常拦截
 */
@ControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(FormWallException.class)
    @ResponseBody
    public R<String> handler(FormWallException e){
        return R.failure(e.getMessage(),e.getCode());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R<String> handler(Exception e){
        return R.failure(e.getMessage(),500);
    }
}
