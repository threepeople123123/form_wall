package com.wj.future.campus.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiToolService {

    /**
     * 根据用户问题搜索文章
     *
     * @param userMessage 用户的问题
     * @return AI 的回答
     */
    @SystemMessage("""
            你是一个校园文章助手，可以帮助用户搜索和查找文章。
            
            重要规则:
            1. 当用户询问关于文章、帖子、内容相关问题时，必须先使用 searchArticles 工具搜索
            2. 根据搜索结果，用自然语言总结并回答用户的问题
            3. 如果搜索结果为空，告诉用户没有找到相关文章
            4. 不要编造不存在的文章内容
            """)
    String chat(@UserMessage String userMessage);
}
