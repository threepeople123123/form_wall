package com.wj.future.compus.controller;


import cn.hutool.core.bean.BeanUtil;
import com.wj.future.compus.entity.pojo.UserPojo;
import com.wj.future.compus.entity.response.UserinfoResponse;
import com.wj.future.compus.exception.FormWallException;
import com.wj.future.compus.result.R;
import com.wj.future.compus.service.UserService;
import com.wj.future.compus.util.UserUtil;
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
    @GetMapping("/queryInfo")
    public R<UserinfoResponse> queryUserinfo(HttpServletRequest request) throws FormWallException {
        UserPojo user = userUtil.getUser(request);
        UserinfoResponse userinfoResponse = BeanUtil.copyProperties(user, UserinfoResponse.class);
        return R.ok(userinfoResponse);
    }

    @PostMapping("/update")
    public R<String> update(){


        return R.ok();
    }

}
