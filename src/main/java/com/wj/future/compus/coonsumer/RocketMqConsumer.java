package com.wj.future.compus.coonsumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.wj.future.compus.entity.nosql.UserToBotConversation;
import com.wj.future.compus.entity.pojo.AiToUserConversationPo;
import com.wj.future.compus.service.AiToUserConversationService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * packageName com.wj.future.compus.coonsumer
 *
 * @author wj
 * @className RocketMqConsumer
 * @date 2026/3/10
 * @description rocketmq消费者
 */
@RocketMQMessageListener(topic = "campus-ai-conversation",consumerGroup = "test-group")
@Component
public class RocketMqConsumer implements RocketMQListener<String> {


    @Autowired
    private AiToUserConversationService aiToUserConversationService;


    @Override
    public void onMessage(String message) {
        //
        UserToBotConversation userToBotConversation = JSONUtil.toBean(message, UserToBotConversation.class);
        AiToUserConversationPo aiToUserConversationPo = BeanUtil.copyProperties(userToBotConversation, AiToUserConversationPo.class);
        aiToUserConversationPo.setId(IdUtil.getSnowflakeNextId());
        aiToUserConversationService.save(aiToUserConversationPo);

    }
}
