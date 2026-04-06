package com.wj.future.campus.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_user")
public class UserPojo {

    @TableId(value = "id",type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @TableField("user_id")
    @Schema(description = "用户id")
    private Long userId;

    @TableField("email")
    @Schema(description = "手机号")
    private String email;

    @TableField("password")
    @Schema(description = "密码")
    private String password;

    @TableField("user_name")
    @Schema(description = "用户名称")
    private String userName;

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
}
