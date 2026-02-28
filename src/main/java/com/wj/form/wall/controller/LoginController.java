package com.wj.form.wall.controller;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wj.form.wall.entity.pojo.UserPojo;
import com.wj.form.wall.entity.request.LoginRequest;
import com.wj.form.wall.exception.FormWallException;
import com.wj.form.wall.result.R;
import com.wj.form.wall.service.UserService;
import com.wj.form.wall.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.wj.form.wall.responseEunm.ResponseEnum.LOGIN_SUCCESS;

@RestController
@RequestMapping("/login")
public class LoginController {


    @Autowired
    private UserService userService;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/login")
    public R<String> login(@RequestBody LoginRequest loginRequest) throws FormWallException {
        String phone = loginRequest.getPhone();
        String password = loginRequest.getPassword();
        String userName = loginRequest.getUserName();
        String deviceType = loginRequest.getDeviceType();
        // 校验参数是否合法
        if (StrUtil.isBlank(phone) || !phone.matches("^1[3-9]\\d{8}$")){
            throw new FormWallException("手机号不合法");
        }
        if (ObjectUtil.isEmpty(password) || password.length()<6){
            throw new FormWallException("密码不能为空，并且要大于等于6位数");
        }
        if (StrUtil.isBlank(userName) || userName.length() >= 50){
            throw new FormWallException("用户名不能为空，并且要小于50字符");
        }

        LambdaQueryWrapper<UserPojo> qw = new LambdaQueryWrapper<>();
        qw.eq(UserPojo::getPhone,phone);
        UserPojo userPojo = userService.getOne(qw);

        Long userId;
        // 登录逻辑
        if (ObjectUtil.isNotEmpty(userPojo)){
            userId = userService.login(loginRequest,userPojo);
        }
        //注册逻辑
        else {
            userId = userService.register(loginRequest);
        }
        // 塞入登录信息
        StpUtil.login(userId,deviceType);

        // todo:测试，查询是否放在threadLocal中
        new Thread(() ->{
            String tokenValue = StpUtil.getTokenValue();
        }).start();
        String tokenValue = StpUtil.getTokenValue();

        userUtil.setUser(tokenValue,userPojo);

        return R.ok(LOGIN_SUCCESS.getMessage(),LOGIN_SUCCESS.getCode());
    }

    @GetMapping("/logout/{deviceType}")
    public void logout(@PathVariable("deviceType") String deviceType){
        long loginIdAsLong = StpUtil.getLoginIdAsLong();

        StpUtil.logout(loginIdAsLong,deviceType);
    }

}
