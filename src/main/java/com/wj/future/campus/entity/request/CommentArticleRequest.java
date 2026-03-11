package com.wj.future.campus.entity.request;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * packageName com.wj.future.campus.entity.request
 *
 * @author wj
 * @className CommentArticleRequest
 * @date 2026/3/11
 * @description 评论或点赞文章
 */
@Data
@Schema(description = "评论或点赞文章")
public class CommentArticleRequest {

    @Schema(description = "文章id")
    private long articleId;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "图片地址")
    private List<String> photoUrl;

    @Schema(description = "点赞数")
    private long likeCount;
}
