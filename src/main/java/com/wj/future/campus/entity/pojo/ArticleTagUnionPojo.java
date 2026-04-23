package com.wj.future.campus.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * packageName com.wj.form.wall.entity.pojo
 *
 * @author wangj
 * @className ArticleTagUnionPojo
 * @date 2026/3/2
 * @description 文章表和标签表联合表
 */
@Data
@TableName("article_tag_union")
public class ArticleTagUnionPojo {
    @TableField("id")
    @Schema(description = "主键")
    private String id;

    @TableField("article_id")
    @Schema(description = "文章id")
    private String articleId;

    @TableField("tag_id")
    @Schema(description = "标签id")
    private String tagId;

    @TableLogic
    @TableField("is_delete")
    private boolean isDelete;
}
