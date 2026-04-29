package com.wj.future.campus.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.wj.future.campus.config.RabbitMQConfig;
import com.wj.future.campus.entity.nosql.UserToBotConversation;
import com.wj.future.campus.entity.pojo.AiToUserConversationPoJo;
import com.wj.future.campus.service.AiToUserConversationService;
import com.wj.future.campus.util.EmbeddingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RabbitMQ 消息消费者
 */
@Slf4j
@Component
public class RabbitMQConsumer {

    @Autowired
    private AiToUserConversationService aiToUserConversationService;

    @Autowired
    private EmbeddingUtil embeddingUtil;

    /**
     * 监听示例队列
     *
     * @param message 消息内容
     * @param channel 通道
     */
    @RabbitListener(queues = RabbitMQConfig.CAMPUS_AI_CONVERSATION_QUEUE)
    public void receiveExampleMessage(String message, Channel channel, Message msg) {
        try {
            log.info("收到消息: {}", message);

            UserToBotConversation userToBotConversation = JSONUtil.toBean(message, UserToBotConversation.class);
            AiToUserConversationPoJo aiToUserConversationPoJo = BeanUtil.copyProperties(userToBotConversation, AiToUserConversationPoJo.class);
            aiToUserConversationPoJo.setId(IdUtil.getSnowflakeNextId());
            aiToUserConversationPoJo.setCreateTime(LocalDateTime.now());
            aiToUserConversationService.save(aiToUserConversationPoJo);

            // 将用户消息和ai回复向量化存储起来
            List<Double> userVector = embeddingUtil.embedToVector(userToBotConversation.getUser());
            List<Double> botVector = embeddingUtil.embedToVector(userToBotConversation.getBot());


            // 手动确认消息（因为配置了 acknowledge-mode: manual）
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("消息确认成功:{}", message);
            
        } catch (Exception e) {
            log.error("处理消息失败", e);
            try {
                // 如果处理失败，拒绝消息并重新入队
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
                log.info("消息已重新入队:{}",message);
            } catch (IOException ioException) {
                log.error("消息拒绝失败", ioException);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CAMPUS_ARTICLE_QUEUE)
    public void receiveArticleMessage(String message, Channel channel, Message msg) {
        try {
            log.info("收到消息: {}", message);
            //  todo:处理文章数据

            // 手动确认消息（因为配置了 acknowledge-mode: manual）
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
            log.info("消息消费成功:{}",message);
        } catch (Exception e) {
            log.error("处理消息失败", e);
            try {
                // 如果处理失败，拒绝消息并重新入队
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
                log.info("消息已重新入队:{}" ,message);
            } catch (IOException ioException) {
                log.error("消息拒绝失败", ioException);
            }
        }
    }
}
