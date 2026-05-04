package com.wj.future.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.future.campus.entity.pojo.rdb.CommentArticlePojo;
import org.apache.ibatis.annotations.Mapper;

/**
 * packageName com.wj.future.campus.mapper
 *
 * @author wj
 * @interfaceName CommentArticleMapper
 * @date 2026/3/11
 * @description 点赞评论
 */
@Mapper
public interface CommentArticleMapper extends BaseMapper<CommentArticlePojo> {
}
