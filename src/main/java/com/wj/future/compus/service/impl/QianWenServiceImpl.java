package com.wj.future.compus.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.History;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.*;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.wj.future.compus.entity.nosql.UserToBotConversation;
import com.wj.future.compus.producer.SendMessageCallbackImpl;
import com.wj.future.compus.properties.ApiKeyProperties;
import com.wj.future.compus.service.AiService;
import io.reactivex.Flowable;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.dashscope.embeddings.TextEmbedding.Models.TEXT_EMBEDDING_V3;
import static com.wj.future.compus.campusEnum.RedisEnum.USER_BOT_TO_CONVERSATION;

@Service("qianWenServiceImpl")
public class QianWenServiceImpl<T> implements AiService<T> {

    @Autowired
    private ApiKeyProperties apiKeyProperties;

    @Autowired
    private RedisTemplate<String,Object> redisTemplateConfig;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public static final Logger logger = LoggerFactory.getLogger(QianWenServiceImpl.class);

    /**
     * 一次性返回所有内容
     * @param msg 用户发送消息
     * @return ai回到的所有内容
     */
    @Override
    public String chatAllContent(String msg, List<T> history) {
        return "";
    }

    /**
     * 通过流的方式进行返回
     *
     * @param msg            用户发送的消息
     * @param knowledgeDoc   知识库检索内容
     * @param conversationId 会话id
     * @return sse链接推送的ai生成内容
     */
    @Override
    public SseEmitter chatForStream(String msg, List<T> history, String knowledgeDoc, String conversationId) {
        SseEmitter sseEmitter = new SseEmitter();
        new Thread(() -> {
            try {
                Generation gen = new Generation();
                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("校园助手，你可以帮人搜帖子，解答各种问题")
                        .reasoningContent(knowledgeDoc)
                        .build();
                Message userMsg = Message.builder()
                        .role(Role.USER.getValue())
                        .content(msg)
                        .build();

                List<History> historyList = null;
                if (CollUtil.isNotEmpty(history)){
                    historyList = history.stream().map(his -> {
                        if (his instanceof History source) {
                            return (History) History.builder().user(source.user).bot(source.bot).build();
                        }
                        return null;
                    }).toList();
                }

                GenerationParam param = GenerationParam.builder()
                        // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                        .apiKey(apiKeyProperties.getQianWenApiKey())
                        // 模型列表：https://help.aliyun.com/model-studio/getting-started/models
                        .model("qwen-plus")
                        .history(historyList)
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .incrementalOutput(true)
                        .build();

                Flowable<GenerationResult> result = gen.streamCall(param);
                StringBuffer aiReply = new StringBuffer();
                result.blockingForEach(res -> {

                    String content = res.getOutput()
                            .getChoices()
                            .get(0)
                            .getMessage()
                            .getContent();

                    try {
                        sseEmitter.send(content);
                        aiReply.append(content);
                        logger.info("消息：{}",content);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                });
                // 对话信息穿入redis
                UserToBotConversation userToBotConversation = new UserToBotConversation();
                userToBotConversation.setBot(aiReply.toString());
                userToBotConversation.setUser(msg);
                userToBotConversation.setConversationId(conversationId);

                //发送mq，落库
                rocketMQTemplate.asyncSend("campus-ai-conversatio", JSONUtil.toJsonStr(userToBotConversation), new SendMessageCallbackImpl(rocketMQTemplate));
                redisTemplateConfig.opsForHash().put(USER_BOT_TO_CONVERSATION.getKey(), conversationId, JSONUtil.toJsonStr(userToBotConversation));
                sseEmitter.complete();
            } catch (NoApiKeyException | InputRequiredException e) {
                throw new RuntimeException(e);
            }
        }).start();
        return sseEmitter;
    }

    /**
     * 模型向量化
     * @param msg 消息
     * @return 向量化之后的消息
     */
    @Override
    public List<Double> embedding(String msg) {
        try {
            MultiModalEmbeddingItemText textContent = new MultiModalEmbeddingItemText(msg);
            List<MultiModalEmbeddingItemBase> contents = Arrays.asList(textContent);

            TextEmbeddingParam textEmbeddingParam = TextEmbeddingParam.builder().apiKey(apiKeyProperties.getQianWenApiKey()).text(msg).model(TEXT_EMBEDDING_V3).dimension(1024).build();
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(textEmbeddingParam);
            return result.getOutput().getEmbeddings().get(0).getEmbedding();

        } catch (NoApiKeyException e) {
            // 捕获并处理API Key未设置的异常
            System.err.println("调用 API 时发生异常: " + e.getMessage());
            System.err.println("请检查您的 API Key 是否已正确配置。");
            e.printStackTrace();
        }
        return null;
    }
}
