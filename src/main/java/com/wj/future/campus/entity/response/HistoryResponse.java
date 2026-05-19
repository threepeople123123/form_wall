package com.wj.future.campus.entity.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HistoryResponse {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "会话ID")
    private String conversationId;
}
