package com.wj.future.campus.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, streamingChatModel = "openAiStreamingChatModel", tools = {"aiArticleTool"},chatMemoryProvider = "chatMemoryStoreHandler")
public interface AiStreamService {

    /**
     * 根据用户问题搜索文章
     *
     * @param userMessage 用户的问题
     * @return AI 的回答
     */
    @SystemMessage("""
            你是一个校园文章助手，可以帮助用户搜索和查找文章。
            
            重要规则:
            1. 当用户询问关于文章、帖子、内容相关问题时，优先调用工具，查询系统内部的文章
            2. 根据搜索结果，用自然语言总结并回答用户的问题
            3. 如果搜索结果为空，可以告诉用户没有找到，然后去查询网上的相关文章
            4. 不要编造不存在的文章内容
            """)
    TokenStream chat(@UserMessage String userMessage);
}
