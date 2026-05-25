package com.future.campus.entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ConversationMessageResponse {

    @Schema(description = "消息id")
    private String id;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息角色")
    private String role;

    @Schema(description = "消息时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timestamp;
}
