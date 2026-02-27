package com.wj.form.wall.entity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SendContentRequest {

    @Schema(description = "内容")
    private String content;

    @Schema(description = "查看范围,1:全部，2:本校")
    private int viewRange;

    @Schema(description = "图片地址")
    private List<String> photoUrl;
}
