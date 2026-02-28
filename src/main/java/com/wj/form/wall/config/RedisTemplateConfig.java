package com.wj.form.wall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * RedisTemplate 配置类
 * 关键：1. 加 @Configuration 让 Spring 扫描到；2. 定义 RedisTemplate Bean 并注入 RedisConnectionFactory
 */

@Configuration // 必须加这个注解，否则 Spring 不会识别为配置类
public class RedisTemplateConfig {

    // 定义 RedisTemplate Bean，泛型用 <String, Object>（适配大部分场景）
    @Bean // 必须加 @Bean 注解，否则不会注册到 Spring 容器
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 注入 Spring Boot 自动创建的 RedisConnectionFactory（从 yml 配置读取参数）
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 配置序列化器（可选，但解决乱码问题，推荐加）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // key 和 hashKey 用 String 序列化
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        // value 和 hashValue 用 JSON 序列化
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
