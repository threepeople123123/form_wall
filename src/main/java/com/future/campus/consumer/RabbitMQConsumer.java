package com.future.campus.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.future.campus.config.RabbitMQConfig;
import com.future.campus.entity.pojo.nosql.SourceVectorIndex;
import com.future.campus.entity.pojo.nosql.UserToBotConversation;
import com.future.campus.entity.pojo.rdb.AiToUserConversationHistoryPojo;
import com.future.campus.entity.pojo.rdb.AiToUserConversationPoJo;
import com.future.campus.mapper.AiToUserConversationHistoryMapper;
import com.future.campus.service.AiSimplifyModelService;
import com.future.campus.service.AiToUserConversationService;
import com.future.campus.util.EmbeddingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.typesense.api.Client;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private Client typeSenseClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AiToUserConversationHistoryMapper aiToUserConversationHistoryMapper;

    @Autowired
    private AiSimplifyModelService aiSimplifyModelService;
    /**
     * 监听示例队列
     *
     * @param message 消息内容
     * @param channel 通道
     */
    @RabbitListener(queues = RabbitMQConfig.CAMPUS_AI_CONVERSATION_QUEUE)
    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public void receiveExampleMessage(String message, Channel channel, Message msg) {
        try {
            log.info("收到消息: {}", message);

            transactionTemplate.execute(transactionStatus -> {
                UserToBotConversation userToBotConversation = JSONUtil.toBean(message, UserToBotConversation.class);
                AiToUserConversationPoJo aiToUserConversationPoJo = BeanUtil.copyProperties(userToBotConversation, AiToUserConversationPoJo.class);
                aiToUserConversationPoJo.setId(IdUtil.getSnowflakeNextId());
                aiToUserConversationPoJo.setCreateTime(LocalDateTime.now());
                boolean save = aiToUserConversationService.save(aiToUserConversationPoJo);

                LambdaQueryWrapper<AiToUserConversationHistoryPojo> qw = new LambdaQueryWrapper<>();
                qw.eq(AiToUserConversationHistoryPojo::getConversationId,aiToUserConversationPoJo.getConversationId());
                qw.last("limit 1");
                AiToUserConversationHistoryPojo aiToUserConversationHistoryPojo = aiToUserConversationHistoryMapper.selectOne(qw);
                if (ObjectUtil.isEmpty(aiToUserConversationHistoryPojo)){

                    String title = userToBotConversation.getBot();

                    if (userToBotConversation.getBot().length() > 15){
                        title = aiSimplifyModelService.chat(userToBotConversation.getUser());
                    }

                    AiToUserConversationHistoryPojo aiToUserConversationHistoryPoJo = new AiToUserConversationHistoryPojo();
                    aiToUserConversationHistoryPoJo.setTitle(title);
                    aiToUserConversationHistoryPoJo.setUserId(userToBotConversation.getUserId());
                    aiToUserConversationHistoryPoJo.setId(IdUtil.getSnowflakeNextId());
                    aiToUserConversationHistoryPoJo.setConversationId(userToBotConversation.getConversationId());
                    aiToUserConversationHistoryPoJo.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    aiToUserConversationHistoryMapper.insert(aiToUserConversationHistoryPoJo);
                }



                // 将用户消息和ai回复向量化存储起来
                List<Double> userVector = embeddingUtil.embedToVector(userToBotConversation.getUser());
                List<Double> botVector = embeddingUtil.embedToVector(userToBotConversation.getBot());

                SourceVectorIndex sourceVectorIndex = new SourceVectorIndex();
                sourceVectorIndex.setBotVectorForDouble(botVector);
                sourceVectorIndex.setUserVectorForDouble(userVector);
                sourceVectorIndex.setBotMsg(userToBotConversation.getBot());
                sourceVectorIndex.setUserMsg(userToBotConversation.getUser());
                sourceVectorIndex.setConversationId(userToBotConversation.getConversationId());
                sourceVectorIndex.setUserId(userToBotConversation.getUserId());
                sourceVectorIndex.setCreateTime(System.currentTimeMillis());

                IKSegmenter ikSegmenter = new IKSegmenter(new StringReader(userToBotConversation.getUser()), false);

                try {
                    List<String> result = new ArrayList<>();
                    Lexeme lexeme;
                    while ((lexeme = ikSegmenter.next()) != null) {
                        result.add(lexeme.getLexemeText());
                    }
                    sourceVectorIndex.setUserMsgSeg(String.join(" ", result));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                ikSegmenter =  new IKSegmenter(new StringReader(userToBotConversation.getBot()), false);

                try {
                    List<String> result = new ArrayList<>();
                    Lexeme lexeme;
                    while ((lexeme = ikSegmenter.next()) != null) {
                        result.add(lexeme.getLexemeText());
                    }
                    sourceVectorIndex.setBotMsgSeg(String.join(" ", result));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

//                sourceVectorIndex.setId(IdUtil.getSnowflakeNextId());

                Map<String, Object> stringObjectMap = BeanUtil.beanToMap(sourceVectorIndex);
                try {
                    typeSenseClient.collections("source_vector").documents().create(stringObjectMap);
                } catch (Exception e) {
                    log.error("向Typesense写入数据失败", e);
                    throw new RuntimeException(e);
                }
                return save;
            });
            // 手动确认消息（因为配置了 acknowledge-mode: manual）
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("消息确认成功:{}", message);
            
        } catch (Exception e) {
            try {
                log.error("处理消息失败", e);
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败", ex);
            }
        }
    }

    /**
     * 当重试次数用尽后，会调用这个恢复方法
     * 在这里将消息推送到死信队列或记录日志
     */
    @Recover
    public void recover(Exception e, Message msg, Channel channel) {
        log.error("❌ 消息重试 3 次后仍然失败，进入死信处理流程", e);
        try {
            // 手动拒绝消息，requeue=false 使其进入死信队列
            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException ioException) {
            log.error("拒绝消息失败", ioException);
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
