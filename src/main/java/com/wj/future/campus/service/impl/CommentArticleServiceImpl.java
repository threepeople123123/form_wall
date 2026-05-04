package com.wj.future.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.future.campus.entity.pojo.rdb.CommentArticlePojo;
import com.wj.future.campus.mapper.CommentArticleMapper;
import com.wj.future.campus.service.CommentArticleService;
import org.springframework.stereotype.Service;

/**
 * packageName com.wj.future.campus.service.impl
 *
 * @author wj
 * @className CommentArticleServiceImpl
 * @date 2026/3/11
 * @description 点赞评论
 */
@Service
public class CommentArticleServiceImpl extends ServiceImpl<CommentArticleMapper, CommentArticlePojo> implements CommentArticleService {
}
