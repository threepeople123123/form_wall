package com.future.campus.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.future.campus.entity.pojo.rdb.UserPojo;
import com.future.campus.entity.request.AiConversationRequest;
import com.future.campus.properties.ApiKeyProperties;
import com.future.campus.service.CampusAiService;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("zhiPuServiceImpl")
public class ZhiPuServiceImpl<T> implements CampusAiService<T> {

    public static final Logger logger = LoggerFactory.getLogger(ZhiPuServiceImpl.class);

    @Autowired
    private ApiKeyProperties apiKeyProperties;

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
     * @param aiConversationRequest ai对话所需参数
     * @return sse链接推送的ai生成内容
     */
    @Override
    public SseEmitter chatForSEE(AiConversationRequest aiConversationRequest) {
        String conversationId = aiConversationRequest.getConversationId();
        String msg = aiConversationRequest.getMsg();
        List<T> histories = aiConversationRequest.getHistories();
        String knowledgeDoc = aiConversationRequest.getKnowledgeDoc();
        UserPojo user = aiConversationRequest.getUserPojo();

        SseEmitter sseEmitter = new SseEmitter();
        new Thread(() -> {
            try {
                Generation gen = new Generation();
                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("ai助手")
                        .build();
                Message userMsg = Message.builder()
                        .role(Role.USER.getValue())
                        .content(msg)
                        .build();
                GenerationParam param = GenerationParam.builder()
                        // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                        .apiKey(apiKeyProperties.getZhiPu().getZhiPuApiKey())
                        // 模型列表：https://help.aliyun.com/model-studio/getting-started/models
                        .model("qwen-plus")
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .incrementalOutput(true)
                        .build();

                Flowable<GenerationResult> result = gen.streamCall(param);
                result.blockingForEach(res -> {

                    String content = res.getOutput()
                            .getChoices()
                            .get(0)
                            .getMessage()
                            .getContent();

                    try {
                        sseEmitter.send(content);
                        logger.info("消息：{}",content);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                });
                sseEmitter.complete();
            } catch (NoApiKeyException | InputRequiredException e) {
                throw new RuntimeException(e);
            }
        }).start();
        return sseEmitter;
    }

    @Override
    public List<Double> embedding(String msg) {
        return new ArrayList<>();
    }
}
