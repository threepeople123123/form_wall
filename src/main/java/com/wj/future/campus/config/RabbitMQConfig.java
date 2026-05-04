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
     * ai对话死信队列名称
     */
    public static final String CAMPUS_AI_CONVERSATION_DLQ = "campus.ai.conversation.dlq";
    
    /**
     * ai对话死信路由键
     */
    public static final String CAMPUS_AI_CONVERSATION_DLQ_ROUTING_KEY = "campus.ai.conversation.dlq.routing.key";


    /**
     * 文章队列队列名称
     */
    public static final String CAMPUS_ARTICLE_QUEUE = "campus.article.queue";

    /**
     * 文章队列路由键
     */
    public static final String CAMPUS_ARTICLE_ROUTING_KEY = "campus.article.routing.key";

    /**
     * 文章死信队列名称
     */
    public static final String CAMPUS_ARTICLE_DLQ = "campus.article.dlq";

    /**
     * 文章死信路由键
     */
    public static final String CAMPUS_ARTICLE_DLQ_ROUTING_KEY = "campus.article.dlq.routing.key";

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
     * 创建队列（带死信配置）
     * 当消息消费失败超过重试次数后，会自动转发到死信队列
     */
    @Bean
    public Queue campusAiConversationQueue() {
        return QueueBuilder
                .durable(CAMPUS_AI_CONVERSATION_QUEUE)
                // 配置死信交换机
                .deadLetterExchange(CAMPUS_EXCHANGE)
                // 配置死信路由键
                .deadLetterRoutingKey(CAMPUS_AI_CONVERSATION_DLQ_ROUTING_KEY)
                // 消息在队列中的最大存活时间（可选，这里设置为1小时）
                // .ttl(3600000)
                // 最大重试次数前的延迟时间（可选）
                // .xMessageTtl(60000)
                .build();
    }

    /**
     * 创建 AI 对话死信队列
     * 用于存储消费失败的消息，便于后续人工处理或重新消费
     */
    @Bean
    public Queue campusAiConversationDlq() {
        return QueueBuilder
                .durable(CAMPUS_AI_CONVERSATION_DLQ)
                .build();
    }

    /*
    文章队列（带死信配置）
     */
    @Bean
    public Queue campusArticleQueue() {
        return QueueBuilder
                .durable(CAMPUS_ARTICLE_QUEUE)
                // 配置死信交换机
                .deadLetterExchange(CAMPUS_EXCHANGE)
                // 配置死信路由键
                .deadLetterRoutingKey(CAMPUS_ARTICLE_DLQ_ROUTING_KEY)
                .build();
    }

    /**
     * 创建文章死信队列
     */
    @Bean
    public Queue campusArticleDlq() {
        return QueueBuilder
                .durable(CAMPUS_ARTICLE_DLQ)
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
     * 绑定文章死信队列到交换机
     */
    @Bean
    public Binding campusArticleDlqBinding(Queue campusArticleDlq, Exchange exchange) {
        return BindingBuilder
                .bind(campusArticleDlq)
                .to(exchange)
                .with(CAMPUS_ARTICLE_DLQ_ROUTING_KEY)
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

    /**
     * 绑定 AI 对话死信队列到交换机
     */
    @Bean
    public Binding campusAiConversationDlqBinding(Queue campusAiConversationDlq, Exchange exchange) {
        return BindingBuilder
                .bind(campusAiConversationDlq)
                .to(exchange)
                .with(CAMPUS_AI_CONVERSATION_DLQ_ROUTING_KEY)
                .noargs();
    }
}
