package com.wj.future.campus.producer;

import com.wj.future.campus.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息生产者
 */
@Component
public class RabbitMQProducer {
    
    public static final Logger logger = LoggerFactory.getLogger(RabbitMQProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到示例队列
     *
     * @param message 消息内容
     */
    public void sendCampusAiConversationMessage(String message) {
        logger.info("发送消息到交换机: {}, 路由键: {}, 消息内容: {}", 
                RabbitMQConfig.CAMPUS_EXCHANGE, 
                RabbitMQConfig.CAMPUS_AI_CONVERSATION_ROUTING_KEY,
                message);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAMPUS_EXCHANGE,
                RabbitMQConfig.CAMPUS_AI_CONVERSATION_ROUTING_KEY,
                message
        );

        logger.info("消息发送成功");
    }

    /**
     * 发送对象消息（会自动序列化为 JSON）
     *
     * @param object 消息对象
     */
    public void sendCampusAiConversationObjectMessage(Object object) {
        logger.info("发送对象消息到交换机: {}, 路由键: {}", 
                RabbitMQConfig.CAMPUS_EXCHANGE, 
                RabbitMQConfig.CAMPUS_AI_CONVERSATION_ROUTING_KEY);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAMPUS_EXCHANGE,
                RabbitMQConfig.CAMPUS_AI_CONVERSATION_ROUTING_KEY,
                object
        );
        
        logger.info("对象消息发送成功");
    }

    /**
     * 发送消息到示例队列
     *
     * @param message 消息内容
     */
    public void sendCampusArticleMessage(String message) {
        logger.info("发送消息到交换机: {}, 路由键: {}, 消息内容: {}",
                RabbitMQConfig.CAMPUS_EXCHANGE,
                RabbitMQConfig.CAMPUS_ARTICLE_ROUTING_KEY,
                message);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAMPUS_EXCHANGE,
                RabbitMQConfig.CAMPUS_ARTICLE_ROUTING_KEY,
                message
        );

        logger.info("消息发送成功");
    }

    /**
     * 发送对象消息（会自动序列化为 JSON）
     *
     * @param object 消息对象
     */
    public void sendCampusArticleObjectMessage(Object object) {
        logger.info("发送对象消息到交换机: {}, 路由键: {}",
                RabbitMQConfig.CAMPUS_EXCHANGE,
                RabbitMQConfig.CAMPUS_ARTICLE_ROUTING_KEY);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAMPUS_EXCHANGE,
                RabbitMQConfig.CAMPUS_ARTICLE_ROUTING_KEY,
                object
        );

        logger.info("对象消息发送成功");
    }
}
