package com.wj.future.campus.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.wj.future.campus.annotation.AuthIsLogin;
import com.wj.future.campus.entity.pojo.CommentArticlePojo;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.request.CommentArticleRequest;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.result.R;
import com.wj.future.campus.service.CommentArticleService;
import com.wj.future.campus.util.UserUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @Resource
    private CommentArticleService commentArticleService;

    /*
    发送评论
     */
    @AuthIsLogin
    @PostMapping("/sendComment")
    public R<String> sendComment(@RequestBody CommentArticleRequest commentArticleRequest, HttpServletRequest request) throws FormWallException {
        List<String> photoUrl = commentArticleRequest.getPhotoUrl();

        String content = commentArticleRequest.getContent();
        boolean likeCount = commentArticleRequest.isLikeCount();
        if (StrUtil.isBlank(content)){
            throw new FormWallException("点赞或者评论");
        }

        if (CollUtil.isNotEmpty(photoUrl) && photoUrl.size() > 9){
            throw new FormWallException("图片最多不超过九张");
        }

        UserPojo userPojo = userUtil.getUser(request);

        CommentArticlePojo commentArticlePojo = new CommentArticlePojo();
        // 判断是否已经点过赞，判断是否没有点过赞

        // 直接保存
//        commentArticleService.save()

        return R.ok("发送成功");
    }

}
