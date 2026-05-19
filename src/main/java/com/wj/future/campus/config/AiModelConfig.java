package com.wj.future.campus.config;

import com.wj.future.campus.aiTools.AllTools;
import com.wj.future.campus.handler.ChatMemoryStoreHandler;
import com.wj.future.campus.properties.ApiKeyProperties;
import com.wj.future.campus.rag.TypesenseVectorContentRetriever;
import com.wj.future.campus.service.AiStreamService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModelConfig {

    @Autowired
    ApiKeyProperties apiKeyProperties;

    @Autowired
    private ChatMemoryStoreHandler chatMemoryStoreHandler;

    @Autowired
    private TypesenseVectorContentRetriever typesenseVectorContentRetriever;

    @Autowired
    private AllTools allTools;


    @Bean
    public AiStreamService openAiStreamingChatModel() {
        OpenAiStreamingChatModel openAiStreamingChatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(apiKeyProperties.getGateway().getUrl())
                .apiKey(apiKeyProperties.getGateway().getLitellmMasterKey())
                .modelName(apiKeyProperties.getDashscope().getQwen3_5_122b_a10b()).build();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(chatMemoryStoreHandler)
                .build();

        return AiServices.builder(AiStreamService.class)
                .streamingChatModel(openAiStreamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(typesenseVectorContentRetriever)  // 添加向量检索器
                .tools(allTools.getAllTools().toArray())  // 自动注册所有工具
                .systemMessage("""
                        你是一个校园文章助手，可以帮助用户搜索和查找文章，需要使用俚语进行回答。
                        重要规则:
                        1. 当用户询问关于文章、帖子、内容相关问题时，优先调用工具，查询系统内部的文章
                        2. 根据搜索结果，用自然语言总结并回答用户的问题
                        3. 如果搜索结果为空，可以告诉用户系统中没有找到，然后去查询网上的相关文章
                        4. 不要编造不存在的文章内容
                        """)
                .build();
    }

    @Bean
    public OpenAiImageModel openAiImageModel() {
        return OpenAiImageModel.builder()
                .baseUrl(apiKeyProperties.getGateway().getUrl())
                .apiKey(apiKeyProperties.getGateway().getLitellmMasterKey())
                .modelName(apiKeyProperties.getDashscope().getDashscope_qwen_image_2_0()).build();
    }

}
