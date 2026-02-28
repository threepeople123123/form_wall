package com.wj.form.wall.exceptionHandler;

import com.wj.form.wall.exception.FormWallException;
import com.wj.form.wall.result.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public static final Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);

    @ExceptionHandler(FormWallException.class)
    @ResponseBody
    public R<String> handler(FormWallException e){
        logger.error("异常信息：",e);
        return R.failure(e.getMessage(),e.getCode());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R<String> handler(Exception e){
        logger.error("异常信息：",e);
        return R.failure("服务器正在整备中，请稍后",500);
    }
}
