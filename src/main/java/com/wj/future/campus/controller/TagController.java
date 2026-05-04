package com.wj.future.campus.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wj.future.campus.entity.pojo.rdb.TagPojo;
import com.wj.future.campus.entity.response.TagResponse;
import com.wj.future.campus.result.R;
import com.wj.future.campus.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
        查询热度最高的10条标签
     */
    @GetMapping("/getPopularTag")
    public R<List<TagResponse>> getPopularTag(){
        LambdaQueryWrapper<TagPojo> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(TagPojo::getHot);
        qw.last("limit 10");
        List<TagPojo> tagPojoList = tagService.list(qw);
        if (CollUtil.isNotEmpty(tagPojoList)){
            List<TagResponse> tagResponses = BeanUtil.copyToList(tagPojoList, TagResponse.class);
            return R.ok(tagResponses);
        }
        return R.ok();
    }
}
