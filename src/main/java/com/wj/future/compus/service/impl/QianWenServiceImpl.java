package com.wj.future.compus.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.History;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.*;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.wj.future.compus.properties.ApiKeyProperties;
import com.wj.future.compus.service.AiService;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service("qianWenServiceImpl")
public class QianWenServiceImpl<T> implements AiService<T> {

    @Autowired
    private ApiKeyProperties apiKeyProperties;

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
     * @param msg          用户发送的消息
     * @param knowledgeDoc 知识库检索内容
     * @return sse链接推送的ai生成内容
     */
    @Override
    public SseEmitter chatForStream(String msg, List<T> history, String knowledgeDoc) {
        SseEmitter sseEmitter = new SseEmitter();
        new Thread(() -> {
            try {
                Generation gen = new Generation();
                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("ai助手")
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
            MultiModalEmbeddingParam param = MultiModalEmbeddingParam.builder()
                    .model("qwen3-vl-embedding")
                    .apiKey(apiKeyProperties.getQianWenApiKey())
                    .contents(contents)
                    .build();
            MultiModalEmbedding multiModalEmbedding = new MultiModalEmbedding();
            MultiModalEmbeddingResult result = multiModalEmbedding.call(param);

            // 输出结果
            System.out.println(result);

            return result.getOutput().getEmbeddings().get(0).getEmbedding();

        } catch (NoApiKeyException e) {
            // 捕获并处理API Key未设置的异常
            System.err.println("调用 API 时发生异常: " + e.getMessage());
            System.err.println("请检查您的 API Key 是否已正确配置。");
            e.printStackTrace();
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
