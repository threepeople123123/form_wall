package com.wj.future.campus.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.core.health_report.MasterIsStableIndicatorClusterFormationNode;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.History;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.*;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.wj.future.campus.entity.nosql.UserToBotConversation;
import com.wj.future.campus.producer.SendMessageCallbackImpl;
import com.wj.future.campus.properties.ApiKeyProperties;
import com.wj.future.campus.service.AiService;
import dev.ai4j.openai4j.chat.UserMessage;
import io.reactivex.Flowable;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.dashscope.embeddings.TextEmbedding.Models.TEXT_EMBEDDING_V3;
import static com.wj.future.campus.campusEnum.RedisEnum.USER_BOT_TO_CONVERSATION;

@Service("qianWenServiceImpl")
public class QianWenServiceImpl<T> implements AiService<T> {

    public static final Logger logger = LoggerFactory.getLogger(QianWenServiceImpl.class);

    @Autowired
    private ApiKeyProperties apiKeyProperties;

    @Autowired
    private RedisTemplate<String,Object> redisTemplateConfig;

//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;



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

                List<Message> messages = new ArrayList<>();

                List<UserToBotConversation> userToBotConversations = new ArrayList<>();

                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("校园助手，你可以帮人搜帖子，解答各种问题")
                        .reasoningContent(knowledgeDoc)
                        .build();
                messages.add(systemMsg);

                if (CollUtil.isNotEmpty(history)){
                    for (T t : history) {
                        if (t instanceof History source){
                            Message userMsg = Message.builder()
                                    .role(Role.USER.getValue())
                                    .content(source.user)
                                    .build();
                            Message botMsg = Message.builder()
                                    .role(Role.ASSISTANT.getValue())
                                    .content(source.bot)
                                    .build();
                            messages.add(userMsg);
                            messages.add(botMsg);
                            UserToBotConversation userToBotConversation = new UserToBotConversation();
                            userToBotConversation.setBot(source.bot);
                            userToBotConversation.setUser(source.user);
                            userToBotConversations.add(userToBotConversation);
                        }
                    }
                }

                Message userMsg = Message.builder()
                        .role(Role.USER.getValue())
                        .content(msg)
                        .build();
                messages.add(userMsg);

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
                        .model("tongyi-xiaomi-analysis-pro")
                        .history(historyList)
                        .messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .incrementalOutput(true)
                        .build();

                Generation gen = new Generation();
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

                userToBotConversations.add(userToBotConversation);

                //todo：后期可以改成rabbitmq
//                rocketMQTemplate.asyncSend("campus-ai-conversatio", JSONUtil.toJsonStr(userToBotConversation), new SendMessageCallbackImpl(rocketMQTemplate));
                redisTemplateConfig.opsForHash().put(USER_BOT_TO_CONVERSATION.getKey(), conversationId, JSONUtil.toJsonStr(userToBotConversations));
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
            logger.error("{},",e);
        }
        return null;
    }
}
