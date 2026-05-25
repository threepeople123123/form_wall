package com.future.campus.entity.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SearchConversationResponse {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "对话id")
    private String conversationId;

    @Schema(description = "用户提的问题内容")
    private String user;

    @Schema(description = "ai回复的内容")
    private String bot;

    @Schema(description = "标题")
    private String title;
}