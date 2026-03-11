package com.wj.future.campus.producer;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessageCallbackImpl implements SendCallback {

    public static final Logger logger = LoggerFactory.getLogger(SendMessageCallbackImpl.class);
    private final RocketMQTemplate rocketMQTemplate;

     public SendMessageCallbackImpl(RocketMQTemplate rocketMQTemplate){
        this.rocketMQTemplate =rocketMQTemplate;
    }

    @Override
    public void onSuccess(SendResult sendResult) {
         logger.info("发送成功");
    }

    @Override
    public void onException(Throwable throwable) {
        logger.error("发送失败");

    }
}
