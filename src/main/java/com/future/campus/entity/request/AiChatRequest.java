package com.future.campus.entity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AiChatRequest {
    @Schema(description = "用户输入的文本")
    private String msg;

    @Schema(description = "会话ID")
    private String conversationId;
}
