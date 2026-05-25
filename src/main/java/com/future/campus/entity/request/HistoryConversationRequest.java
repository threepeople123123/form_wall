package com.future.campus.entity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HistoryConversationRequest {

    @Schema(description = "页码")
    private int pageNum = 1;

    @Schema(description = "页大小")
    private int pageSize = 10;
}

