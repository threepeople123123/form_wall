package com.wj.future.compus.coonsumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

/**
 * packageName com.wj.future.compus.coonsumer
 *
 * @author wj
 * @className RocketMqConsumer
 * @date 2026/3/10
 * @description rocketmq消费者
 */
@RocketMQMessageListener(topic = "test-topic",consumerGroup = "test-group")
public class RocketMqConsumer{

}
