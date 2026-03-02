package com.wj.future.compus.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
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

    @TableField("tag")
    @Schema(description = "标签名称")
    private String tag;
}
