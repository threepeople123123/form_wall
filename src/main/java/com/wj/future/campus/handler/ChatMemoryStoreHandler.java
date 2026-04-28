package com.wj.future.campus.handler;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.wj.future.campus.campusEnum.RedisEnum.USER_BOT_TO_CONVERSATION;
import static org.bouncycastle.asn1.x500.style.RFC4519Style.o;

@Component
public class ChatMemoryStoreHandler implements ChatMemoryStore {

    public static final Logger logger = LoggerFactory.getLogger(ChatMemoryStoreHandler.class);

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String result =(String) redisTemplate.opsForHash().get(String.valueOf(USER_BOT_TO_CONVERSATION), memoryId);
        return (List<ChatMessage>) o;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        for (ChatMessage chatMessage : list) {
            ChatMessageType type = chatMessage.type();
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        logger.error("删除记忆");
    }
}
