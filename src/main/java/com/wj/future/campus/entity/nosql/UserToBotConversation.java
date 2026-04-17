package com.wj.future.campus.entity.nosql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class UserToBotConversation {
    @Schema(description = "ai回复的内容")
    private String bot;

    @Schema(description = "用户提的问题内容")
    private String user;

    @Schema(description = "对话id")
    private String conversationId;

    @Schema(description = "用户id")
    private Long userId;
}
