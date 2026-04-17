package com.wj.future.campus.config;

import com.wj.future.campus.aiTools.AiArticleTool;
import com.wj.future.campus.properties.ApiKeyProperties;
import com.wj.future.campus.service.AiService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiToolConfig {

    @Autowired
    ApiKeyProperties apiKeyProperties;

    @Bean
    public AiService articleAiAssistant(AiArticleTool articleTools) {
        // 1. 初始化模型（这里以 OpenAI 为例）
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKeyProperties.getQianWenApiKey())
                .modelName(apiKeyProperties.getTongyiXiaomiAnalysisPro())
                .build();

        // 2. 绑定工具 - 注意：必须传入接口，不能传入实现类
        return AiServices.builder(AiService.class)
                .chatLanguageModel(model)
                .tools(articleTools) // 注入上面定义的工具类
                .build();
    }
}
