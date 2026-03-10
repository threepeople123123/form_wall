package com.wj.future.compus.util;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;

public class AiUtil {
    public static final Logger logger = LoggerFactory.getLogger(AiUtil.class);

    public static final String AIP_KEY = "sk-0363d3e4787e4ab19253e56309e0ff95";
    public static void systemContext(String context, SseEmitter emitter) {

        new Thread(() -> {
            try {
                Generation gen = new Generation();
                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("ai助手")
                        .build();
                Message userMsg = Message.builder()
                        .role(Role.USER.getValue())
                        .content(context)
                        .build();
                GenerationParam param = GenerationParam.builder()
                        // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                        .apiKey(AIP_KEY)
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
                        emitter.send(content);
                        logger.info("消息：{}",content);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (NoApiKeyException | InputRequiredException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
