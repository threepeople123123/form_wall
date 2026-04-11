package com.wj.future.campus.controller;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wj.future.campus.campusEnum.RSAConst;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.request.LoginRequest;
import com.wj.future.campus.entity.request.RegisterRequest;
import com.wj.future.campus.entity.request.ResetPasswordRequest;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.result.R;
import com.wj.future.campus.service.UserService;
import com.wj.future.campus.util.EmailUtil;
import com.wj.future.campus.util.MySecurityUtil;
import com.wj.future.campus.util.RSAUtils;
import com.wj.future.campus.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import static com.wj.future.campus.responseEunm.ResponseEnum.LOGIN_SUCCESS;
import static com.wj.future.campus.responseEunm.ResponseEnum.REST_PASSWORD_FAIL;

@RestController
@RequestMapping("/login")
public class LoginController {

    public static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private EmailUtil emailUtil;

    @PostMapping("/login")
    public R<String> login(@RequestBody LoginRequest loginRequest) throws FormWallException {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        String deviceType = loginRequest.getDeviceType();
        // 校验参数是否合法
        if (StrUtil.isBlank(email) || !email.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")){
            throw new FormWallException("邮箱不合法");
        }
        if (ObjectUtil.isEmpty(password) || password.length()<6){
            throw new FormWallException("密码不能为空，并且要大于等于6位数");
        }
        LambdaQueryWrapper<UserPojo> qw = new LambdaQueryWrapper<>();
        qw.eq(UserPojo::getEmail,email);
        UserPojo userPojo = userService.getOne(qw);

        if (ObjectUtil.isEmpty(userPojo)){
            throw new FormWallException("邮箱不存在");
        }

        try {
            password = RSAUtils.decrypt(password, RSAConst.PRIVATE_KEY);
            loginRequest.setPassword(password);
        }catch (Exception e){
            //
            throw new FormWallException("请通过页面请求接口");
        }

        // 登录逻辑
        userService.login(loginRequest,userPojo);

        // 塞入登录信息
        StpUtil.login(userPojo.getUserId());

        String tokenValue = StpUtil.getTokenValue();

        userUtil.setUser(tokenValue,userPojo);

        return R.ok(tokenValue,"登录成功");
    }

    @PostMapping("/register")
    public R<String> register(@RequestBody RegisterRequest registerRequest) throws FormWallException {
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();
        String userName = registerRequest.getUserName();
        String verificationCode = registerRequest.getVerificationCode();
        // 校验参数是否合法
        if (StrUtil.isBlank(email) || !email.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")){
            throw new FormWallException("邮箱不合法");
        }
        if (ObjectUtil.isEmpty(password) || password.length()<6){
            throw new FormWallException("密码不能为空，并且要大于等于6位数");
        }
        if (StrUtil.isBlank(userName) || userName.length() > 30 || userName.length() < 2){
            throw new FormWallException("用户名不能为空，并且要大于2小于30字符");
        }
        if (StrUtil.isBlank(verificationCode)){
            throw  new FormWallException("验证码不能为空");
        }
        Boolean exit = redisTemplate.hasKey("emailCode:" + email);
        if (exit){
            String redisCode = (String) redisTemplate.opsForValue().get("emailCode:" + email);
            if (!verificationCode.equals(redisCode)){
                throw new FormWallException("验证码错误");
            }
        }else {
            throw new FormWallException("验证码已过期");
        }

        try {
            password = RSAUtils.decrypt(password, RSAConst.PRIVATE_KEY);
            registerRequest.setPassword(password);

            confirmPassword = RSAUtils.decrypt(confirmPassword, RSAConst.PRIVATE_KEY);
            registerRequest.setConfirmPassword(confirmPassword);

            if (!password.equals(confirmPassword)){
                throw new FormWallException("密码不一致");
            }
        }catch (Exception e){
            //
            throw new FormWallException("请通过页面请求接口");
        }

        // 判断邮箱和用户名是否存在，不能存在相同的用户名
        LambdaQueryWrapper<UserPojo> qw = new LambdaQueryWrapper<>();
        qw.eq(UserPojo::getEmail,email).or().eq(UserPojo::getUserName,userName);
        qw.last("limit 1");
        UserPojo userPojo = userService.getOne(qw);
        if (ObjectUtil.isNotEmpty(userPojo)){
            if (userPojo.getEmail().equals(email)){
                throw new FormWallException("邮箱已存在");
            }
            if (userPojo.getUserName().equals(userName)){
                throw new FormWallException("用户名已存在");
            }
        }

        userService.register(registerRequest);

        return R.ok(LOGIN_SUCCESS.getMessage(),LOGIN_SUCCESS.getCode());
    }


    @PostMapping("/resetPassword")
    public R<String> restPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) throws FormWallException {
    // Extract request parameters
        String confirmPassword = resetPasswordRequest.getConfirmPassword();
        String password = resetPasswordRequest.getPassword();
        String email = resetPasswordRequest.getEmail();
        String verificationCode = resetPasswordRequest.getVerificationCode();
    // Validate email format
        if (StrUtil.isBlank(email) || !email.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")){
            throw new FormWallException("邮箱不合法");
        }
    // Validate password requirements
        if (ObjectUtil.isEmpty(password) || password.length()<6){
            throw new FormWallException("密码不能为空，并且要大于等于6位数");
        }
    // Check if confirm password is provided
        if (StrUtil.isBlank(confirmPassword)){
            throw new FormWallException("请输入确认密码");
        }
    // Check if verification code is provided
        if (StrUtil.isBlank(verificationCode)){
            throw  new FormWallException("验证码不能为空");
        }
    // Verify the email code in Redis
        Boolean exit = redisTemplate.hasKey("emailCode:" + email);
        if (exit){
            String redisCode = (String) redisTemplate.opsForValue().get("emailCode:" + email);
            if (!verificationCode.equals(redisCode)){
                throw new FormWallException("验证码错误");
            }
        }else {
            throw new FormWallException("验证码已过期");
        }

        try {
            // 解密
            password = RSAUtils.decrypt(password, RSAConst.PRIVATE_KEY);
            resetPasswordRequest.setPassword(password);

            confirmPassword = RSAUtils.decrypt(confirmPassword, RSAConst.PRIVATE_KEY);
            resetPasswordRequest.setConfirmPassword(confirmPassword);

            if (!password.equals(confirmPassword)){
                throw new FormWallException("密码不一致");
            }
        }catch (Exception e){
            //
            throw new FormWallException("请通过页面请求接口");
        }
        LambdaQueryWrapper<UserPojo> qw = new LambdaQueryWrapper<>();
        qw.eq(UserPojo::getEmail,email);
        qw.last("limit 1");
        UserPojo userPojo = userService.getOne(qw);
        if (ObjectUtil.isEmpty(userPojo)){
            throw new FormWallException("用户不存在");
        }
        LambdaUpdateWrapper<UserPojo> uw = new LambdaUpdateWrapper();
        uw.set(UserPojo::getPassword, MySecurityUtil.md5Security(password));
        uw.eq(UserPojo::getEmail,userPojo.getEmail());
        boolean update = userService.update(uw);
        return  update ? R.ok("重置密码成功",LOGIN_SUCCESS.getCode()) : R.failure(REST_PASSWORD_FAIL.getMessage(),REST_PASSWORD_FAIL.getCode());
    }

    @GetMapping("/logout/{deviceType}")
    public void logout(@PathVariable("deviceType") String deviceType){
        long loginIdAsLong = StpUtil.getLoginIdAsLong();

        StpUtil.logout(loginIdAsLong,deviceType);
    }

    @GetMapping("/sendCode")
    private R<String> sendCode(@RequestParam("email")String email) throws FormWallException {

        if (StrUtil.isBlank(email) || !email.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")){
            throw new FormWallException("邮箱不合法");
        }

        try {
            // 生成验证码,六位数验证码
            String code = RandomUtil.randomNumbers(6);

            // 保存验证码
            redisTemplate.opsForValue().set("emailCode:"+email,code,5, TimeUnit.MINUTES);

            // 给用户发送邮箱code
            emailUtil.sendSimpleMail(email,"未来校园验证码","验证码："+code);

            return R.okMsg("发送成功，请注意查收验证码，时效五分钟");
        }catch (Exception e){
            logger.error("{}",e);
            throw new FormWallException("发送失败,请检查邮箱是否输入正确");
        }
    }

}
