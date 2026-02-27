package com.wj.form.wall.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wj.form.wall.entity.pojo.UserPojo;
import com.wj.form.wall.exception.FormWallException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * packageName com.wj.form.wall.util
 *
 * @author wj
 * @className UserUtil
 * @date 2026/2/27
 * @description 用户工具类
 */
@Component
public class UserUtil {

    public static final String USER_KEY = "form:wall:user";

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    public String getToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            String token = cookie.getAttribute("token");
            if (StrUtil.isNotBlank(token)){
                return token;
            }
        }
        return null;
    }


    public UserPojo setUser(String token,UserPojo userPojo) throws FormWallException {
        userPojo.setPassword(null);
        redisTemplate.opsForHash().put(USER_KEY,token,JSONUtil.toJsonStr(userPojo));
        throw new FormWallException("用户未登录");
    }

    public UserPojo getUser(HttpServletRequest request) throws FormWallException {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            String token = cookie.getAttribute("token");
            if (StrUtil.isNotBlank(token)){
                String userJson = (String)redisTemplate.opsForHash().get(USER_KEY, token);
                return JSONUtil.toBean(userJson, UserPojo.class);
            }
        }
        throw new FormWallException("用户未登录");
    }
}
