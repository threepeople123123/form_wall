package com.wj.future.campus.config;

import com.wj.future.campus.properties.ApiKeyProperties;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModelConfig {

    @Autowired
    ApiKeyProperties apiKeyProperties;


    @Bean
    public OpenAiStreamingChatModel openAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(apiKeyProperties.getQianWenBaseUrl())
                .apiKey(apiKeyProperties.getQianWenApiKey())
                .modelName(apiKeyProperties.getQwen3_5_plus()).build();
    }

}
