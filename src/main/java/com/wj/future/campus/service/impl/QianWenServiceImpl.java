package com.wj.future.campus.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolBase;
import com.alibaba.dashscope.tools.ToolFunction;
import com.wj.future.campus.aiTools.AllTools;
import com.wj.future.campus.aiTools.ToolInterface;
import com.wj.future.campus.entity.pojo.nosql.UserToBotConversation;
import com.wj.future.campus.entity.pojo.rdb.UserPojo;
import com.wj.future.campus.entity.request.AiConversationRequest;
import com.wj.future.campus.producer.RabbitMQProducer;
import com.wj.future.campus.properties.ApiKeyProperties;
import com.wj.future.campus.service.CampusAiService;
import io.reactivex.Flowable;
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
public class QianWenServiceImpl<T> implements CampusAiService<T> {

    public static final Logger logger = LoggerFactory.getLogger(QianWenServiceImpl.class);

    @Autowired
    private ApiKeyProperties apiKeyProperties;

    @Autowired
    private RedisTemplate<String,Object> redisTemplateConfig;

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    @Autowired
    private AllTools allTools;



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
     * @param aiConversationRequest ai对话参数
     * @return sse链接推送的ai生成内容
     */
    @Override
    public SseEmitter chatForSEE(AiConversationRequest aiConversationRequest) {
        String conversationId = aiConversationRequest.getConversationId();
        String msg = aiConversationRequest.getMsg();
        String knowledgeDoc = aiConversationRequest.getKnowledgeDoc();
        List<T> history = aiConversationRequest.getHistories();
        UserPojo userPojo = aiConversationRequest.getUserPojo();
        SseEmitter sseEmitter = new SseEmitter();
        new Thread(() -> {
            try {

                List<Message> messages = new ArrayList<>();

                List<UserToBotConversation> userToBotConversations = new ArrayList<>();

                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("你是校园助手,你可以帮助用户搜索帖子、查找校园文章等")
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

                // 配置工具列表
                List<ToolInterface> toolInterfaces = allTools.getAllTools();
                List<ToolBase> toolBaseList = new ArrayList<>();
                for (ToolInterface tool : toolInterfaces) {
                    FunctionDefinition definition = tool.definition();
                    ToolFunction toolFunction = ToolFunction.builder()
                            .function(definition)
                            .build();
                    toolBaseList.add(toolFunction);
                }

                GenerationParam param = GenerationParam.builder()
                        // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                        .apiKey(apiKeyProperties.getDashscope().getQianWenApiKey())
                        // 模型列表：https://help.aliyun.com/model-studio/getting-started/models
                        .model(apiKeyProperties.getDashscope().getQwen3_5_122b_a10b())
                        .history(historyList)
                        .messages(messages)
//                        .tools(toolBaseList)
                        .toolChoice("auto") // 自动选择是否调用工具
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .incrementalOutput(true)
                        .build();

                Generation gen = new Generation();
                StringBuffer aiReply = new StringBuffer();

                Flowable<GenerationResult> secondResult = gen.streamCall(param);

//                Message outputMessage = gen.getOutput().getChoices().get(0).getMessage();
//                if (CollUtil.isNotEmpty(outputMessage.getToolCalls())){
//                    for (ToolCallBase toolCallBase : outputMessage.getToolCalls()) {
//
//                    }
//                }


                secondResult.blockingForEach(res -> {

                    // 没有工具调用，直接返回内容
                    String content = res.getOutput()
                            .getChoices()
                            .get(0)
                            .getMessage()
                            .getContent();


                    if (content != null && !content.isEmpty()) {
                        try {
                            sseEmitter.send(content);
                            aiReply.append(content);
                            logger.info("消息：{}", content);
                        } catch (IOException e) {
                            sseEmitter.completeWithError(e);
                        }
                    }
                });

                // 对话信息穿入redis
                UserToBotConversation userToBotConversation = new UserToBotConversation();
                userToBotConversation.setBot(aiReply.toString());
                userToBotConversation.setUser(msg);
                userToBotConversation.setConversationId(conversationId);
                if (ObjectUtil.isNotEmpty(userPojo)){
                    userToBotConversation.setUserId(userPojo.getId());
                }

                userToBotConversations.add(userToBotConversation);

                // 先发送mq，防止redis数据丢失
                rabbitMQProducer.sendCampusAiConversationMessage(JSONUtil.toJsonStr(userToBotConversation));

                redisTemplateConfig.opsForHash().put(USER_BOT_TO_CONVERSATION.getKey(), conversationId, JSONUtil.toJsonStr(userToBotConversations));


            } catch (NoApiKeyException | InputRequiredException e) {
                throw new RuntimeException(e);
            } finally {
                sseEmitter.complete();
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

            TextEmbeddingParam textEmbeddingParam = TextEmbeddingParam.builder().apiKey(apiKeyProperties.getDashscope().getQianWenApiKey()).text(msg).model(TEXT_EMBEDDING_V3).dimension(1024).build();
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
