package com.wj.form.wall.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.wj.form.wall.entity.pojo.UserPojo;
import com.wj.form.wall.entity.request.SendContentRequest;
import com.wj.form.wall.exception.FormWallException;
import com.wj.form.wall.result.R;
import com.wj.form.wall.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/send")
public class ForwardController {

    @Autowired
    private UserUtil userUtil;

    @PostMapping("/content")
    public R<String> content(@RequestBody SendContentRequest sendContentRequest , HttpServletRequest request) throws FormWallException {
        String content = sendContentRequest.getContent();
        List<String> photoUrl = sendContentRequest.getPhotoUrl();
        int viewRange = sendContentRequest.getViewRange();
        if (StrUtil.isBlank(content)){
            throw new FormWallException("内容不能为空");
        }
        if (CollUtil.isNotEmpty(photoUrl) && photoUrl.size() > 9){
            throw new FormWallException("最多不超过9张图片");
        }
        UserPojo user = userUtil.getUser(request);
        if (viewRange == 2){
            if (StrUtil.isBlank(user.getSchoolId())){
                throw new FormWallException("请先绑定学校");
            }
        }




        return R.ok();
    }
}
