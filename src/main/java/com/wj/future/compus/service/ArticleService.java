package com.wj.future.compus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.future.compus.entity.pojo.ArticlePojo;
import com.wj.future.compus.entity.request.ArticleRequest;
import com.wj.future.compus.entity.response.ArticleResponse;

/**
 * packageName com.wj.form.wall.service
 *
 * @author wangj
 * @interfaceName ArticleService
 * @date 2026/3/2
 * @description article 表服务
 */
public interface ArticleService extends IService<ArticlePojo> {
    /***
     * 查询分页列表
     * @param articleRequest 查询条件
     * @return 列表数据
     */
    Page<ArticleResponse> pageArticleList(ArticleRequest articleRequest);
}
