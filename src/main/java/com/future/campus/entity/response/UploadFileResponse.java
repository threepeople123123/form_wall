package com.future.campus.entity.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UploadFileResponse {

    @Schema(description = "文件id")
    private Long id;

    @Schema(description = "下载地址")
    private String downloadUrl;
}
