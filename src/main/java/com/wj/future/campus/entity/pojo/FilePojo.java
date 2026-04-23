package com.wj.future.campus.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Data
@TableName("file")
public class FilePojo {
    @TableId("id")
    private long id;

    @TableField("correlation_id")
    private String correlationId;

    @TableField("user_id")
    private long userId;

    @TableField("user_name")
    private String userName;

    @TableField("file_name")
    private String fileName;

    @TableField("content_type")
    private String contentType;

    @TableField("download_url")
    private String downloadUrl;

    @TableField("size")
    private double size;

    @TableField("object_name")
    private String objectName;

    @TableField("bucket_name")
    private String bucketName;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableLogic
    @TableField("is_delete")
    private boolean isDelete;
}
