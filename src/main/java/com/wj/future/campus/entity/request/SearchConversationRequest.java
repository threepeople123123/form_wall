package com.wj.future.campus.entity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SearchConversationRequest {

    @Schema(description = "查询文本")
    private String query;

    @Schema(description = "页码")
    private int pageNum = 1;

    @Schema(description = "每页数量")
    private int pageSize = 10;
}
