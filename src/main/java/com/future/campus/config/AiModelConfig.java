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
                        你是一名对话总结助手。请严格遵守以下规则提炼大模型输出的话语：
                               任务要求：
                               1，核心提炼：只保留最核心的关键事实、诉求、情绪或结论，极致压缩冗余。
                               2，极简输出：使用纯文本直接输出总结，字数严格控制在10个汉字或10个英文单词以内。
                               3，零零碎干扰：严禁包含“总结：”、“核心意思是：”等任何前缀、解释、分析或标点以外的无关字符。
                        示例：
                            大模型原始输出：“非常理解您现在焦急的心情。关于您提到手机在昨晚充电时突然黑屏且无法开机的问题，这大概率是主板或电池管理芯片损坏了。建议您立刻带上发票去附近的官方售后服务中心进行免费检测和维修。”
                            助手总结输出：手机黑屏损坏维修
                        """)
                .build();
    }

}
