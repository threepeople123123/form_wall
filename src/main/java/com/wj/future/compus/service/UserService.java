package com.wj.future.compus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.future.compus.entity.request.LoginRequest;
import com.wj.future.compus.entity.pojo.UserPojo;
import com.wj.future.compus.exception.FormWallException;

/**
 * packageName com.wj.form_wall
 *
 * @author wj
 * @interfaceName UserService
 * @date 2026/2/27
 * @description 用户服务接口
 */
public interface UserService extends IService<UserPojo> {
    /**
     * 登录逻辑
     * @param loginRequest 用户输入的登录信息
     * @param userPojo 用户信息
     * @return 用户id
     */
    Long login(LoginRequest loginRequest, UserPojo userPojo) throws FormWallException;

    /**
     * 注册逻辑
     * @param loginRequest 用户输入的注册信息
     * @return 用户id
     */
    Long register(LoginRequest loginRequest);
}
