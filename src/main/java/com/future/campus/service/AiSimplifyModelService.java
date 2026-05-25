package com.future.campus.service;

import dev.langchain4j.service.UserMessage;

public interface AiSimplifyModelService {

    /**
     * 总结话语服务
     * @param userMessage
     * @return
     */
    String chat(@UserMessage String userMessage);
}
