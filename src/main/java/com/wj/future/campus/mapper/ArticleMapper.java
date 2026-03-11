package com.wj.future.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.future.campus.entity.pojo.ArticlePojo;
import com.wj.future.campus.entity.request.ArticleRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * packageName com.wj.form.wall.mapping
 *
 * @author wangj
 * @interfaceName ArticleMapper
 * @date 2026/3/2
 * @description 内容mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<ArticlePojo> {

    /**
     * 查询分页列表
     * @param articleRequest 条件
     * @return 文章列表
     */
    List<ArticlePojo> pageArticleList(@Param("articleRequest") ArticleRequest articleRequest);

    /**
     * 查询分页列表数量
     * @param articleRequest 条件
     * @return 数量
     */
    int pageArticleListCount(@Param("articleRequest") ArticleRequest articleRequest);
}
