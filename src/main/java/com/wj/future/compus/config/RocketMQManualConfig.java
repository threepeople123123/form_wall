package com.wj.future.compus.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketMQManualConfig {

//    @Value("${rocketmq.name-server}")
//    private String nameServer;
//
//    @Value("${rocketmq.producer.group}")
//    private String producerGroup;
//
//    @Bean
//    public RocketMQTemplate rocketMQTemplate() {
//        RocketMQTemplate template = new RocketMQTemplate();
//        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
//        producer.setNamesrvAddr(nameServer);
//        // 5.x 建议设置，防止某些环境下的心跳问题
//        producer.setSendMsgTimeout(3000);
//
//        template.setProducer(producer);
//        return template;
//    }
}