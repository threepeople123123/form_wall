package com.future.campus.entity.pojo.rdb;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * packageName com.wj.form.wall.entity.pojo
 *
 * @author wangj
 * @className TagPojo
 * @date 2026/3/2
 * @description 标签表
 */
@Data
@TableName("tag")
public class TagPojo {
    @TableField("id")
    @Schema(description = "标签id")
    private String id;

    @TableField("tag_name")
    @Schema(description = "标签名称")
    private String tagName;

    @TableField("hot")
    @Schema(description = "标签热度")
    public int hot;

    @TableLogic(value = "false",delval = "true")
    @TableField("is_delete")
    private boolean isDelete;
}
