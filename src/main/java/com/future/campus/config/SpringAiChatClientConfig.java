package com.future.campus.config;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeConnectionProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class SpringAiChatClientConfig {


    @Value("spring.ai.dashscope.baseUrl")
    private String baseUrl;

    @Value("spring.ai.dashscope.apiKey")
    private String apiKey;

//    @Bean("dashScopeChatClient")
//    public ChatClient dashscopeChatModel() {
//
//        DashScopeApi dashScopeApi = new DashScopeApi(baseUrl,);
//        ChatModel myChatModel = new DashScopeChatModel()
//        ChatClient chatClient = ChatClient.create(myChatModel);
//
//        // 或使用 Builder 实现更精细控制
//        ChatClient.Builder builder = ChatClient.builder(myChatModel);
//        ChatClient customChatClient = builder
//                .defaultSystemPrompt("You are a helpful assistant.")
//                .build();
//    }

    /*@Configuration
    public class ChatClientConfig {

        @Bean
        ChatClient dashScopeChatClient(
                DashScopeConnectionProperties connectionProps,
                ObjectProvider<ObservationRegistry> observationRegistry,
                ObjectProvider<RetryTemplate> retryTemplate,
                ObjectProvider<ToolCallingManager> toolCallingManager,
                ObjectProvider<ToolExecutionEligibilityPredicate> toolExecutionEligibilityPredicates) {

            DashScopeApi api = DashScopeApi.builder()
                    .apiKey(connectionProps.getApiKey())
                    .build();

            DashScopeChatOptions options = DashScopeChatOptions.builder()
                    .withModel("qwen-plus")
                    .withTemperature(0.7)
                    .build();

            DashScopeChatModel chatModel = new DashScopeChatModel(
                    api,
                    options,
                    toolCallingManager.getIfAvailable(),
                    retryTemplate.getIfAvailable(),
                    observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                    toolExecutionEligibilityPredicates.getIfAvailable()
            );

            return ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
        }
    }*/




}
