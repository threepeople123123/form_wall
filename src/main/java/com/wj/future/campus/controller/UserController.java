package com.wj.future.campus.controller;


import cn.hutool.core.bean.BeanUtil;
import com.wj.future.campus.checkLogin.AuthIsLogin;
import com.wj.future.campus.entity.pojo.rdb.UserPojo;
import com.wj.future.campus.entity.request.UserUpdateRequest;
import com.wj.future.campus.entity.response.UserinfoResponse;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.result.R;
import com.wj.future.campus.service.UserService;
import com.wj.future.campus.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName com.wj.form.wall.controller
 *
 * @author wj
 * @className UserController
 * @date 2026/2/27
 * @description 用户信息操作接口
 */
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private UserService userService;

    /**
     * 查询用户信息
     * @param request 请求对象
     * @return 用户信息
     * @throws FormWallException 自定义异常
     */
    @AuthIsLogin
    @GetMapping("/info")
    public R<UserinfoResponse> queryUserinfo(HttpServletRequest request) throws FormWallException {
        UserPojo user = userUtil.getUser(request);
        UserinfoResponse userinfoResponse = BeanUtil.copyProperties(user, UserinfoResponse.class);
        userinfoResponse.setUserId(user.getId());
        userinfoResponse.setUserName(user.getName());
        return R.ok(userinfoResponse);
    }

    @AuthIsLogin
    @PostMapping("/update")
    public R<String> update(UserUpdateRequest userUpdateRequest, HttpServletRequest request) throws FormWallException {
        UserPojo userPojo = userUtil.getUser(request);
        BeanUtil.copyProperties(userUpdateRequest, userPojo);
        userService.updateById(userPojo);


        return R.ok();
    }

}
