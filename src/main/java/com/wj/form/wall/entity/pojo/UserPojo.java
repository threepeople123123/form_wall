package com.wj.form.wall.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class UserPojo {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("phone")
    private String phone;

    @TableField("password")
    private String password;

    @TableField("user_name")
    private String userName;

    @TableField("user_head_url")
    private String userHeadUrl;

    @TableField("school_name")
    private String schoolName;

    @TableField("school_id")
    private String schoolId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
