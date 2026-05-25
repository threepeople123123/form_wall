package com.future.campus.config;

import com.future.campus.aiTools.AllTools;
import com.future.campus.handler.ChatMemoryStoreHandler;
import com.future.campus.mcp.CampusMcpUtil;
import com.future.campus.properties.ApiKeyProperties;
import com.future.campus.rag.TypesenseVectorContentRetriever;
import com.future.campus.service.AiSimplifyModelService;
import com.future.campus.service.AiStreamService;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
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

    @Autowired
    private CampusMcpUtil campusMcpUtil;


    @Bean("openAiStreamingChatModel")
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

        // 2. 创建 MCP 工具提供者
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(campusMcpUtil.getMcpClients())
                // 可选：如果你只想让 AI 使用特定的几个工具，可以配置白名单
                // .filterToolNames("get_weather", "search_map")
                .build();

        return AiServices.builder(AiStreamService.class)
                .streamingChatModel(openAiStreamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(typesenseVectorContentRetriever)  // 添加向量检索器
                .tools(allTools.getAllTools().toArray())  // 自动注册所有工具
                .toolProvider(toolProvider)
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


    @Bean
    public AiSimplifyModelService aiSimplifyModelService() {
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .baseUrl(apiKeyProperties.getGateway().getUrl())
                .apiKey(apiKeyProperties.getGateway().getLitellmMasterKey())
                .modelName(apiKeyProperties.getDashscope().getQwen3_5_122b_a10b()).build();


        return AiServices.builder(AiSimplifyModelService.class)
                .chatModel(openAiChatModel)
                .systemMessage("""
                        你是一名“对话总结助手”。
                               任务要求：
                               1，读取大模型输出的话语。
                               2，提炼核心意思，压缩冗余表达。
                               3，保留关键事实、诉求、情绪和结论。
                               4，使用简洁自然的一句话或几句话输出。
                               5，不添加解释、不分析、不回复用户、不扩展内容。
                               6，只返回总结后的内容。
                               7，禁止出现“总结：”“用户意思是”等前缀。
                               8，禁止输出与总结无关的任何文字。
                               9，总结的话术尽量简短
                               10, 总结的字数不超过十个汉字或者英语单词
                        """)
                .build();
    }

}
