package com.wj.future.campus.entity.request;

import com.wj.future.campus.entity.pojo.rdb.UserPojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class AiConversationRequest<T> {
    @Schema(description = "用户信息")
    private UserPojo userPojo;

    @Schema(description = "对话id")
    private String conversationId;

    @Schema(description = "消息")
    private String msg;

    @Schema(description = "历史消息")
    private List<T> histories;

    @Schema(description = "知识库")
    private String knowledgeDoc;
}
