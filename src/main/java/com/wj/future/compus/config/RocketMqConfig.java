package com.wj.future.compus.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * packageName com.wj.future.compus.config
 *
 * @author wj
 * @className RocketMqConfig
 * @date 2026/3/10
 * @description rocketmq配置
 */
@Configuration
public class RocketMqConfig {

    public RocketMQTemplate rocketMQTemplate() {
        return new RocketMQTemplate();
    }
}
