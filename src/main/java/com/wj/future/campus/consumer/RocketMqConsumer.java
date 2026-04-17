//package com.wj.future.campus.consumer;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.util.IdUtil;
//import cn.hutool.json.JSONUtil;
//import com.wj.future.campus.entity.nosql.UserToBotConversation;
//import com.wj.future.campus.entity.pojo.AiToUserConversationPoJo;
//import com.wj.future.campus.service.AiToUserConversationService;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * packageName com.wj.future.campus.coonsumer
// *
// * @author wj
// * @className RocketMqConsumer
// * @date 2026/3/10
// * @description rocketmq消费者
// */
//@RocketMQMessageListener(topic = "campus-ai-conversation",consumerGroup = "test-group")
//@Component
//public class RocketMqConsumer implements RocketMQListener<String> {
//
//
//    @Autowired
//    private AiToUserConversationService aiToUserConversationService;
//
//
//    @Override
//    public void onMessage(String message) {
//        //
//        UserToBotConversation userToBotConversation = JSONUtil.toBean(message, UserToBotConversation.class);
//        AiToUserConversationPoJo aiToUserConversationPo = BeanUtil.copyProperties(userToBotConversation, AiToUserConversationPoJo.class);
//        aiToUserConversationPo.setId(IdUtil.getSnowflakeNextId());
//        aiToUserConversationService.save(aiToUserConversationPo);
//
//    }
//}
