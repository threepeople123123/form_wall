package com.future.campus.memory;

import cn.hutool.core.collection.CollUtil;
import com.future.campus.campusEnum.RedisEnum;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisMemory implements ChatMemory, StreamAdvisor {


    private RedisTemplate<String,Object> redisTemplate;

    public RedisMemory(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public static final int MEMORY_SIZE = 20;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isNotEmpty(messages) && messages.size() > MEMORY_SIZE){
            for (Message message : messages) {
                if (MessageType.TOOL.equals(message.getMessageType())) {
                    String text = message.getText();

                }
            }
        }

        redisTemplate.opsForValue().set(RedisEnum.USER_BOT_TO_CONVERSATION.getKey() + conversationId,messages,7, TimeUnit.DAYS);
    }

    @Override
    public List<Message> get(String conversationId) {

        Object result = redisTemplate.opsForValue().get(RedisEnum.USER_BOT_TO_CONVERSATION.getKey() + conversationId);
        if (result instanceof List<?>){

        }
        return new ArrayList<>();
    }

    @Override
    public void clear(String conversationId) {

    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return null;
    }

    @Override
    public String getName() {
        return "redisMemory";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
