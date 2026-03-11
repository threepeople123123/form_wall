package com.wj.future.compus.entity.nosql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class userToBotConversation {
    @Schema(description = "ai回复的内容")
    private String bot;

    @Schema(description = "用户提的问题内容")
    private String user;
}
