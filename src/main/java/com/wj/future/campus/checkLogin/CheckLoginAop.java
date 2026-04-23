package com.wj.future.campus.checkLogin;

import com.wj.future.campus.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 登录校验切面
 * 拦截带有 @AuthIsLogin 注解的方法，验证用户是否已登录
 */
@Aspect
@Component
public class CheckLoginAop {

    @Autowired
    private UserUtil userUtil;
    
    /**
     * 环绕通知：拦截带有 @AuthIsLogin 注解的方法
     * @param joinPoint 连接点
     * @throws Throwable 异常
     */
    @Before("@annotation(authIsLogin)")
    public void checkLogin(JoinPoint joinPoint, AuthIsLogin authIsLogin) throws Throwable {
        // 检查用户是否登录
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        userUtil.getUser(request);
    }
}
