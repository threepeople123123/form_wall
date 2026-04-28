package com.wj.future.campus.handler;

import cn.hutool.core.collection.CollUtil;
import com.wj.future.campus.campusEnum.RedisEnum;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChatMemoryStoreHandler implements ChatMemoryStore {

    public static final Logger logger = LoggerFactory.getLogger(ChatMemoryStoreHandler.class);

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private static final Map<String,List<ChatMessage>> map = new HashMap<>();

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        List<ChatMessage> chatMessageList = (List<ChatMessage>) redisTemplate.opsForHash().get(RedisEnum.USER_BOT_TO_CONVERSATION.getKey(), memoryId.toString());

        if (CollUtil.isNotEmpty(chatMessageList)) {
            try {
                // 使用 LangChain4j 的 Json 工具和 TypeToken 进行泛型转换
                return chatMessageList;
            } catch (Exception e) {
                logger.error("反序列化聊天记忆失败, memoryId: {}", memoryId, e);
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        try {
            redisTemplate.opsForHash().put(RedisEnum.USER_BOT_TO_CONVERSATION.getKey(), memoryId.toString(), list);
        } catch (Exception e) {
            logger.error("序列化聊天记忆失败, memoryId: {}", memoryId, e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        logger.error("删除记忆");
    }
}
