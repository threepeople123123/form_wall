package com.wj.future.compus.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiService<T> {

    /**
     * ai一次性返回所有信息
     * @param msg 用户发送的消息
     * @return 一次性返回所有
     */
    String chatAllContent(String msg, List<T> history);

    /**
     * 流式返回
     *
     * @param msg          用户小心
     * @param knowledgeDoc
     * @return sse返回的消息
     */
    SseEmitter chatForStream(String msg, List<T> histroy, String knowledgeDoc);

    /**
     * 模型向量化
     * @param msg 消息
     * @return 向量化之后的消息
     */
    List<Double> embedding(String msg);
}
