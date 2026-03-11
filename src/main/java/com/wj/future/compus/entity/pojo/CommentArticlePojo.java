package com.wj.future.compus.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * packageName com.wj.future.compus.entity.pojo
 *
 * @author wj
 * @className CommentArticlePojo
 * @date 2026/3/11
 * @description 文章评论表
 */
@Data
@TableName("comment_article")
public class CommentArticlePojo {
    @Schema(description = "主键")
    @TableId(type = IdType.ASSIGN_ID)
    private long id;

    @Schema(description = "文章id")
    @TableField("article_id")
    private long articleId;

    @Schema(description = "用户id")
    @TableField("user_id")
    private long userId;

    @Schema(description = "用户名称")
    @TableField("user_name")
    private String userName;

    @Schema(description = "内容")
    @TableField("content")
    private String content;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "点赞数")
    @TableField("like_count")
    private long likeCount;

    @Schema(description = "图片地址")
    @TableField("photo_url")
    private String photoUrl;
}
