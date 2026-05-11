package com.wj.future.campus.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.wj.future.campus.checkLogin.AuthIsLogin;
import com.wj.future.campus.entity.pojo.nosql.UserToBotConversation;
import com.wj.future.campus.entity.pojo.rdb.AiToUserConversationPoJo;
import com.wj.future.campus.entity.pojo.rdb.UserPojo;
import com.wj.future.campus.entity.request.AiChatRequest;
import com.wj.future.campus.entity.request.HistoryConversationRequest;
import com.wj.future.campus.entity.request.SearchConversationRequest;
import com.wj.future.campus.entity.response.SearchConversationResponse;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.producer.RabbitMQProducer;
import com.wj.future.campus.result.R;
import com.wj.future.campus.service.AiStreamService;
import com.wj.future.campus.service.AiToUserConversationService;
import com.wj.future.campus.util.UserUtil;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.typesense.model.MultiSearchCollectionParameters;
import org.typesense.model.MultiSearchSearchesParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/ai")
@ApiSupport(order = 1, author = "wj")
public class AiController {
    public static final Logger logger = LoggerFactory.getLogger(AiController.class);

    public static final ThreadLocal<UserPojo> threadLocalUserPojo = new ThreadLocal<>();

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private AiStreamService aiStreamService;

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    @Autowired
    private AiToUserConversationService aiToUserConversationService;


    @PostMapping("/chat")
    @ApiOperationSupport(order = 1, author = "wj")
    public SseEmitter chat(@RequestBody AiChatRequest aiChatRequest, HttpServletRequest request) {

        AtomicReference<UserPojo> userPojoAtomicReference = new AtomicReference<>(null);
        try {
            UserPojo userPojo = userUtil.getUser(request);
            userPojoAtomicReference.set(userPojo);
        }catch (Exception e){
            logger.error("用户没有登录");
        }
        // 前置判断，需要那种ai进行对话
        


        // 获取用户 ID 或会话 ID 作为 memoryId
        String conversationId = aiChatRequest.getConversationId();

        TokenStream tokenStream = aiStreamService.chat(conversationId, aiChatRequest.getMsg());

        SseEmitter sseEmitter = new SseEmitter();
        tokenStream
                .onPartialResponse((String partialResponse) -> {
                    try {
                        // 使用 event().data() 并指定媒体类型为 text/plain，避免 Jackson 介入
                        sseEmitter.send(SseEmitter.event()
                                .name("message")
                                .data(partialResponse, MediaType.TEXT_PLAIN));
                        logger.info("消息：{}",partialResponse);
                    } catch (IOException e) {
                        logger.error("发送SSE消息失败", e);
                        sseEmitter.completeWithError(e);
                    }
                })
                .onPartialThinking((PartialThinking partialThinking) -> {
                    logger.info("【思考过程】: {}", partialThinking);
                })
                .onRetrieved((List<Content> contents) -> {
                    logger.info("【检索内容】: {}, 数量: {}", contents, contents.size());
                })
                .onIntermediateResponse((ChatResponse intermediateResponse) -> {
                    logger.info("【中间响应】: {}", intermediateResponse);
                })
                // 这将在工具执行之前调用。BeforeToolExecution 包含 ToolExecutionRequest（例如工具名称、工具参数等）
                .beforeToolExecution((BeforeToolExecution beforeToolExecution) -> {
                    threadLocalUserPojo.set(userPojoAtomicReference.get());
                    logger.info("【工具执行前】: {}", beforeToolExecution);
                })
                // 这将在工具执行之后调用。ToolExecution 包含 ToolExecutionRequest 和工具执行结果。
                .onToolExecuted((ToolExecution toolExecution) -> {
                    logger.info("【工具执行后】: {}", toolExecution);
                })
                .onCompleteResponse((ChatResponse response) -> {
                    logger.info("【完整响应】: {}", response.aiMessage().text());
                    UserToBotConversation userToBotConversation = new UserToBotConversation();
                    userToBotConversation.setBot(response.aiMessage().text());
                    userToBotConversation.setUser(aiChatRequest.getMsg());
                    userToBotConversation.setConversationId(conversationId);
                    UserPojo userPojo = userPojoAtomicReference.get();
                    if (ObjectUtil.isNotEmpty(userPojo)){
                        userToBotConversation.setUserId(userPojo.getId());
                    }
                    rabbitMQProducer.sendCampusAiConversationMessage(JSONUtil.toJsonStr(userToBotConversation));
                    sseEmitter.complete();
                })
                .onError((Throwable error) -> {
                    logger.error("【错误信息】: ", error);
                    sseEmitter.completeWithError(error);
                })
                .start();
        
        logger.info("TokenStream 已启动，等待异步响应...");
        return sseEmitter;
    }

    @GetMapping("/getHistroyConversation")
    @ApiOperationSupport(order = 2, author = "wj")
    public R<Page<UserToBotConversation>> getHistroyConversation(HistoryConversationRequest historyConversationRequest, HttpServletRequest request) {

        UserPojo userPojo;
        try {
            userPojo = userUtil.getUser(request);
        } catch (FormWallException e) {
            logger.info("用户未登录，暂时历史对话消息");
            return R.ok(new Page<>(1, 10,0));
        }
        Page<AiToUserConversationPoJo> page = new Page<>(historyConversationRequest.getPageNum(), historyConversationRequest.getPageSize());
        LambdaQueryWrapper<AiToUserConversationPoJo> qw = new LambdaQueryWrapper<>();
        qw.eq(AiToUserConversationPoJo::getUserId, userPojo.getId());
        qw.orderByDesc(AiToUserConversationPoJo::getCreateTime);
        Page<AiToUserConversationPoJo> pageResult = aiToUserConversationService.page(page, qw);
        List<AiToUserConversationPoJo> records = pageResult.getRecords();

        Page<UserToBotConversation> userToBotConversationPage = new Page<>(historyConversationRequest.getPageNum(), historyConversationRequest.getPageSize(), pageResult.getTotal());
        if (CollUtil.isNotEmpty(records)){
            userToBotConversationPage.setRecords(BeanUtil.copyToList(records, UserToBotConversation.class));
        }
        return R.ok(userToBotConversationPage);
    }

    @GetMapping("/searchConversation")
    @AuthIsLogin
    @ApiOperationSupport(order = 3, author = "wj")
    public R<Page<SearchConversationResponse>> searchConversation(SearchConversationRequest searchConversationRequest, HttpServletRequest request) throws FormWallException {
        UserPojo user = userUtil.getUser(request);

        Page<SearchConversationResponse> responsePage = aiToUserConversationService.searchConversation(searchConversationRequest,user);

        return R.ok(responsePage);
    }
}
