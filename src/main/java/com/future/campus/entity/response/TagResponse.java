package com.future.campus.entity.response;

import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TagResponse {

    @Schema(description = "标签id")
    private long id;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签热度")
    private int hot;
}
