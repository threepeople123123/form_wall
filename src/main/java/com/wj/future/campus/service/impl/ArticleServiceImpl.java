package com.wj.future.campus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.future.campus.entity.pojo.ArticlePojo;
import com.wj.future.campus.entity.request.ArticleRequest;
import com.wj.future.campus.entity.response.ArticleResponse;
import com.wj.future.campus.mapper.ArticleMapper;
import com.wj.future.campus.service.ArticleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * packageName com.wj.form.wall.service.impl
 *
 * @author wangj
 * @className ArticleServiceImpl
 * @date 2026/3/2
 * @description 实现类
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, ArticlePojo> implements ArticleService {
    
    
    /***
     * 查询分页列表
     * @param articleRequest 查询条件
     * @return 列表数据
     */
    @Override
    public Page<ArticleResponse> pageArticleList(ArticleRequest articleRequest) {
        List<ArticlePojo> articlePojoList  = baseMapper.pageArticleList(articleRequest);
        int count  = baseMapper.pageArticleListCount(articleRequest);

        List<ArticleResponse> articleResponses = BeanUtil.copyToList(articlePojoList, ArticleResponse.class);
        Page<ArticleResponse> page = new Page<>(articleRequest.getPageNum(),articleRequest.getPageSize());
        page.setRecords(articleResponses);
        page.setTotal(count);
        return page;
    }
}
