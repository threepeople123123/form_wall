package com.wj.future.campus.service;

import com.wj.future.campus.entity.request.AiConversationRequest;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface CampusAiService<T> {

    /**
     * ai一次性返回所有信息
     * @param msg 用户发送的消息
     * @return 一次性返回所有
     */
    String chatAllContent(String msg, List<T> history);

    /**
     * 流式返回
     *
     * @param aiConversationRequest     对话信息等等
     * @return sse返回的消息
     */
    SseEmitter chatForSEE(AiConversationRequest aiConversationRequest);

    /**
     * 模型向量化
     * @param msg 消息
     * @return 向量化之后的消息
     */
    List<Double> embedding(String msg);
}
