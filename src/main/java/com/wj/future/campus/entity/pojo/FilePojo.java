package com.wj.future.campus.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file")
public class FilePojo {
    @TableId("id")
    private long id;

    @TableField("correlation_id")
    private String correlationId;

    @TableField("file_name")
    private String fileName;

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
}
