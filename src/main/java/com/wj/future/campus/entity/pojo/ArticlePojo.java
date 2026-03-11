package com.wj.future.campus.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * packageName com.wj.form.wall.entity.pojo
 *
 * @author wangj
 * @className ArticlePojo
 * @date 2026/3/2
 * @description 内容表
 */
@Data
@TableName("article")
public class ArticlePojo {
    @TableField("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @TableField("content")
    @Schema(description = "内容")
    private String content;

    @TableField("view_range")
    @Schema(description = "查看范围,1:全部，2:学校")
    private int viewRange;

    @TableField("send_user_name")
    @Schema(description = "发送用户名称")
    private String sendUserName;

    @TableField("send_user_id")
    @Schema(description = "发送用户id")
    private Long sendUserId;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField("school_id")
    @Schema(description = "学校id")
    private String schoolId;

    @TableField("school_name")
    @Schema(description = "学校名称")
    private String schoolName;

    @TableField("photo_url")
    @Schema(description = "图片地址")
    private String photoUrl;

    @TableField("like_count")
    @Schema(description = "点赞数")
    private int likeCount;

    @TableField("heat")
    @Schema(description = "热度")
    private int heat;
}
