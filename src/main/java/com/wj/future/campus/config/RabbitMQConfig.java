package com.wj.future.campus.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 定义交换机、队列和绑定关系
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 常量定义 ====================
    
    /**
     * 示例交换机名称
     */
    public static final String CAMPUS_EXCHANGE = "campus.exchange";
    
    /**
     * ai对话队列名称
     */
    public static final String CAMPUS_AI_CONVERSATION_QUEUE = "campus.ai.conversation.queue";
    
    /**
     * ai对话路由键
     */
    public static final String CAMPUS_AI_CONVERSATION_ROUTING_KEY = "campus.ai.conversation.routing.key";


    /**
     * 文章队列队列名称
     */
    public static final String CAMPUS_ARTICLE_QUEUE = "campus.article.queue";

    /**
     * 文章队列路由键
     */
    public static final String CAMPUS_ARTICLE_ROUTING_KEY = "campus.article.routing.key";

    // ==================== Bean 定义 ====================

    /**
     * 创建 Topic 交换机
     * Topic 交换机支持模式匹配的路由键
     */
    @Bean
    public Exchange exchange() {
        return ExchangeBuilder
                .topicExchange(CAMPUS_EXCHANGE)
                .durable(true)  // 持久化
                .build();
    }

    /**
     * 创建队列
     */
    @Bean
    public Queue campusAiConversationQueue() {
        return QueueBuilder
                .durable(CAMPUS_AI_CONVERSATION_QUEUE)  // 持久化队列
                .build();
    }

    /*
    文章队列
     */
    @Bean
    public Queue campusArticleQueue() {
        return QueueBuilder
                .durable(CAMPUS_ARTICLE_QUEUE)  // 持久化队列
                .build();
    }

    /*
      文章路由绑定
     */
    @Bean
    public Binding campusArticleBinding(Queue campusArticleQueue, Exchange exchange) {
        return BindingBuilder
                .bind(campusArticleQueue)
                .to(exchange)
                .with(CAMPUS_ARTICLE_ROUTING_KEY)
                .noargs();
    }
    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding campusAiConversationBinding(Queue campusAiConversationQueue, Exchange exchange) {
        return BindingBuilder
                .bind(campusAiConversationQueue)
                .to(exchange)
                .with(CAMPUS_AI_CONVERSATION_ROUTING_KEY)
                .noargs();
    }
}
