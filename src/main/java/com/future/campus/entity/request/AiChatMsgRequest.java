package com.future.campus.entity.request;

import lombok.Data;

@Data
public class AiChatMsgRequest {
    private String msg;

    private String conversationId;
}
