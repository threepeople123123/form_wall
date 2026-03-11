package com.wj.future.campus.controller;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.wj.future.campus.annotation.AuthIsLogin;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.request.CommentArticleRequest;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.result.R;
import com.wj.future.campus.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName com.wj.future.campus.controller
 *
 * @author wj
 * @className CommentArticleController
 * @date 2026/3/11
 * @description 文章点赞
 */
@RestController
@RequestMapping("/comment")
@ApiSupport(order = 3, author = "wj")
public class CommentArticleController {

    @Autowired
    private UserUtil userUtil;


    /*
    发送评论
     */
    @AuthIsLogin
    @PostMapping("/sendComment")
    public R<String> sendComment(@RequestBody CommentArticleRequest commentArticleRequest, HttpServletRequest request) throws FormWallException {
        UserPojo userPojo = userUtil.getUser(request);


        return R.ok("发送成功");
    }

}
