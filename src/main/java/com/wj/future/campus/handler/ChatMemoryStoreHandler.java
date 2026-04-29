package com.wj.future.campus.handler;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.wj.future.campus.campusEnum.RedisEnum;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

/**
 * 基于 Redis 的聊天记忆存储实现
 * 使用 LangChain4j 官方的 ChatMessageSerializer/ChatMessageDeserializer 进行序列化
 */
@Component
public class ChatMemoryStoreHandler implements ChatMemoryStore {

    public static final Logger logger = LoggerFactory.getLogger(ChatMemoryStoreHandler.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取指定 memoryId 的聊天消息列表
     * @param memoryId 记忆ID（通常是用户ID或会话ID）
     * @return 聊天消息列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        try {
            String key = buildKey(memoryId.toString());
            String json = (String) redisTemplate.opsForValue().get(key);
            
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }

            // 使用 LangChain4j 官方反序列化工具
            List<ChatMessage> messages = messagesFromJson(json);
            
            logger.debug("从 Redis 加载记忆, memoryId: {}, 消息数: {}", memoryId, messages.size());
            return messages;
        } catch (Exception e) {
            logger.error("从 Redis 获取聊天记忆失败, memoryId: {}", memoryId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新指定 memoryId 的聊天消息列表
     * @param memoryId 记忆ID
     * @param messages 聊天消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        try {
            String key = buildKey(memoryId.toString());
            
            // 使用 LangChain4j 官方序列化工具将消息转换为 JSON
            String json = messagesToJson(messages);
            
            // 存储到 Redis，设置 7 天过期时间
            redisTemplate.opsForValue().set(key, json, Duration.ofDays(7));
            
            logger.debug("更新 Redis 记忆, memoryId: {}, 消息数: {}", memoryId, messages.size());
        } catch (Exception e) {
            logger.error("更新 Redis 聊天记忆失败, memoryId: {}", memoryId, e);
        }
    }

    /**
     * 删除指定 memoryId 的聊天记忆
     * @param memoryId 记忆ID
     */
    @Override
    public void deleteMessages(Object memoryId) {
    }

    /**
     * 构建 Redis key
     * @param memoryId 记忆ID
     * @return Redis key
     */
    private String buildKey(String memoryId) {
        return RedisEnum.USER_BOT_TO_CONVERSATION.getKey() + ":" + memoryId;
    }
}
