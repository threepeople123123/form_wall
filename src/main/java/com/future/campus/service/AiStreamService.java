package com.future.campus.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AiStreamService {

    /**
     * 根据用户问题搜索文章
     *
     * @param userMessage 用户的问题
     * @return AI 的回答
     */
    @UserMessage("""
            {{userMessage}}
            
            Read the question again: {{userMessage}}
            """)
    TokenStream chat(@MemoryId String memoryId, @V("userMessage") String userMessage);
}
