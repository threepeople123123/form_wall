package com.wj.future.campus.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.wj.future.campus.entity.nosql.UserToBotConversation;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.request.AiChatRequest;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.producer.RabbitMQProducer;
import com.wj.future.campus.service.AiStreamService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@RestController
@RequestMapping("/ai")
public class AiController {
    public static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private AiStreamService aiStreamService;

    @Autowired
    private RabbitMQProducer rabbitMQProducer;


    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody AiChatRequest aiChatRequest, HttpServletRequest request) throws IOException, FormWallException {

        AtomicReference<UserPojo> userPojoAtomicReference = new AtomicReference<>(null);
        try {
            UserPojo userPojo = userUtil.getUser(request);
            userPojoAtomicReference.set(userPojo);
        }catch (Exception e){
            logger.error("用户没有登录");
        }
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




//        logger.info("langChain4j,消息：{}",chat);

//        UserPojo user = null;
//        try {
//            user = userUtil.getUser(request);
//        }catch (Exception e){
//            logger.error("用户没有登录");
//        }
//
//
//        String msg = aiChatRequest.getMsg();
//        String conversationId = aiChatRequest.getConversationId();
//        if (StrUtil.isBlank(msg) && StrUtil.isBlank(conversationId)) {
//            throw new FormWallException("请输入内容");
//        }
//        //取出历史对话信息
//        String redisHistory = (String)redisTemplate.opsForHash().get(USER_BOT_TO_CONVERSATION.getKey(),conversationId);
//        List<History> histories = new ArrayList<>();
//        if (StrUtil.isNotBlank(redisHistory)){
//            List<UserToBotConversation> userToBotConversations = JSONUtil.toList(redisHistory, UserToBotConversation.class);
//            userToBotConversations = userToBotConversations.subList(Math.max(userToBotConversations.size() - 10, 0), userToBotConversations.size());
//            for (UserToBotConversation userToBotConversation : userToBotConversations) {
//                History history = History.builder().bot(userToBotConversation.getBot()).user(userToBotConversation.getUser()).build();
//                histories.add(history);
//            }
//        }
//
//
//        // 用户问题向量化
//        List<Double> embedding = qianWenService.embedding(msg);
//
//        List<Float> embeddingToFloat = embedding.stream().map(item -> Float.valueOf(String.valueOf(item))).toList();
//
//        // 强制构建一个单一的 KnnQuery 对象，而不是使用 lambda 列表
//        KnnQuery knnQuery = new KnnQuery.Builder()
//                .field("embedding")
//                .queryVector(embeddingToFloat)
//                .k(5)
//                .numCandidates(100)
//                .build();
//
//        SearchResponse<KnowledgeDoc> response = elasticsearchClient.search(s -> s
//                        .index("ai_chat_knowledge")
//                        .knn(knnQuery), // 这里传入单个对象，看是否触发 Client 的兼容逻辑
//                KnowledgeDoc.class
//        );
//        List<KnowledgeDoc> knowledgeDocs = response.hits().hits().stream().map(Hit::source).toList();
//        String knowledgeDoc = "";
//        if (CollUtil.isNotEmpty(knowledgeDocs)) {
//            knowledgeDoc = knowledgeDocs.stream().map(KnowledgeDoc::getContent).collect(Collectors.joining(","));
//        }
//
//        AiConversationRequest<History> aiConversationRequest = new AiConversationRequest<>();
//        aiConversationRequest.setConversationId(conversationId);
//        aiConversationRequest.setMsg(msg);
//        aiConversationRequest.setKnowledgeDoc(knowledgeDoc);
//        aiConversationRequest.setUserPojo(user);
//        aiConversationRequest.setHistories(histories);
//        // 使用流式调用方法
//        return qianWenService.chatForSEE(aiConversationRequest);
    }
}
