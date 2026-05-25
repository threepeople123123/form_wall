package com.future.campus.entity.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryResponse {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "会话ID")
    private String conversationId;
}
