package com.future.campus.entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.future.campus.entity.pojo.rdb.FilePojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * packageName com.wj.form.wall.entity.response
 *
 * @author wangj
 * @className ArticleResponse
 * @date 2026/3/2
 * @description 查询内容列表
 */
@Data
public class ArticleResponse {
    @Schema(description = "内容id")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "查看范围,1:全部，2:学校")
    private int viewRange;

    @Schema(description = "发送用户名称")
    private String sendUserName;

    @Schema(description = "发送用户id")
    private Long sendUserId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "图片地址")
    private List<FilePojo> photoUrl;

    @Schema(description = "点赞数量")
    private int likeCount;

    @Schema(description = "热度")
    private int heat;

    @Schema(description = "标签")
    private List<String> tag;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @Schema(description = "学校id")
    private String schoolId;

    @Schema(description = "学校名称")
    private String schoolName;
}
