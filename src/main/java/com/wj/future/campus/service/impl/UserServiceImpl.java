package com.wj.future.campus.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.request.LoginRequest;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.mapper.UserMapper;
import com.wj.future.campus.service.UserService;
import com.wj.future.campus.util.MySecurityUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * packageName com.wj.form_wall.impl
 *
 * @author wj
 * @className UserServiceImpl
 * @date 2026/2/27
 * @description 用户接口实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPojo> implements UserService {

    /**
     * 登录逻辑
     * @param loginRequest 用户输入的登录信息
     * @param userPojo 用户信息
     * @return 用户id
     */
    @Override
    public Long login(LoginRequest loginRequest, UserPojo userPojo) throws FormWallException {
        String password = userPojo.getPassword();
        String md5Security = MySecurityUtil.md5Security(loginRequest.getPassword());
        if (password.equals(md5Security)){
            return userPojo.getUserId();
        }
        throw new FormWallException("密码错误", HttpStatus.HTTP_INTERNAL_ERROR);
    }

    /**
     * 注册逻辑
     * @param loginRequest 用户输入的注册信息
     * @return 用户id
     */
    @Override
    public Long register(LoginRequest loginRequest) {
        // 随机生成字符串
        long snowflakeNextId = IdUtil.getSnowflakeNextId();

        UserPojo userPojo = new UserPojo();
        userPojo.setUserId(snowflakeNextId);
        userPojo.setUserName(loginRequest.getUserName());
        userPojo.setPhone(loginRequest.getPhone());
        userPojo.setPassword(MySecurityUtil.md5Security(loginRequest.getPassword()));
        userPojo.setCreateTime(LocalDateTime.now());
        userPojo.setUpdateTime(LocalDateTime.now());
        save(userPojo);

        return snowflakeNextId;
    }
}
