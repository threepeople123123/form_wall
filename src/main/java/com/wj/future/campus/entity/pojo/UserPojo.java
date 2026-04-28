package com.wj.future.campus.entity.pojo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_user")
public class UserPojo {

    @TableId(value = "id",type = IdType.AUTO)
    @Schema(description = "主键，用户id")
    private Long id;

    @TableField("email")
    @Schema(description = "邮箱")
    private String email;

    @TableField("password")
    @Schema(description = "密码")
    private String password;

    @TableField("name")
    @Schema(description = "用户名称")
    private String name;

    @TableField("user_head_url")
    @Schema(description = "用户头像")
    private String userHeadUrl;

    @TableField("school_name")
    @Schema(description = "学校名称")
    private String schoolName;

    @TableField("school_id")
    @Schema(description = "学校id")
    private String schoolId;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic(value = "false",delval = "true")
    @TableField("is_delete")
    private boolean isDelete;
}
