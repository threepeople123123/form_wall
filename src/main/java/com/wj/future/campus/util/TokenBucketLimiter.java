package com.wj.future.campus.util;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;

/**
 * 分布式令牌桶限流器
 */
@Component
public class TokenBucketLimiter {

    private final RedissonClient redissonClient;

    private static final String LUA_SCRIPT =
            "local limit_key = KEYS[1] " +
                    "local rate = tonumber(ARGV[1]) " +
                    "local capacity = tonumber(ARGV[2]) " +
                    "local now = tonumber(ARGV[3]) " +
                    "local requested = tonumber(ARGV[4]) " +
                    "local bucket = redis.call('HMGET', limit_key, 'last_time', 'curr_tokens') " +
                    "local last_time = tonumber(bucket[1]) " +
                    "local curr_tokens = tonumber(bucket[2]) " +
                    "if last_time == nil then " +
                    "    last_time = now " +
                    "    curr_tokens = capacity " +
                    "end " +
                    "local delta_time = math.max(0, now - last_time) " +
                    "local generated_tokens = delta_time * rate " +
                    "curr_tokens = math.min(capacity, curr_tokens + generated_tokens) " +
                    "local result = 0 " +
                    "if curr_tokens >= requested then " +
                    "    curr_tokens = curr_tokens - requested " +
                    "    result = 1 " +
                    "end " +
                    "redis.call('HMSET', limit_key, 'last_time', now, 'curr_tokens', curr_tokens) " +
                    "redis.call('EXPIRE', limit_key, 60) " +
                    "return result";

    public TokenBucketLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试获取令牌
     *
     * @param key       限流维度
     * @param rate      每秒填充速率
     * @param capacity  桶容量（最大突发量）
     * @return 是否允许通过
     */
    public boolean tryAcquire(String key, double rate, long capacity) {
        // 使用当前秒级时间戳
        long now = Instant.now().getEpochSecond();

        // 执行 Lua 脚本
        Long result = redissonClient.getScript().eval(
                RScript.Mode.READ_WRITE,
                LUA_SCRIPT,
                RScript.ReturnType.INTEGER,
                Collections.singletonList("limiter:" + key),
                rate, capacity, now, 1
        );

        return result != null && result == 1;
    }
}