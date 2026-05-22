package com.wj.future.campus.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.wj.future.campus.checkLogin.AuthIsLogin;
import com.wj.future.campus.entity.pojo.nosql.UserToBotConversation;
import com.wj.future.campus.entity.pojo.rdb.AiToUserConversationHistoryPojo;
import com.wj.future.campus.entity.pojo.rdb.AiToUserConversationPoJo;
import com.wj.future.campus.entity.pojo.rdb.UserPojo;
import com.wj.future.campus.entity.request.AiChatRequest;
import com.wj.future.campus.entity.request.HistoryConversationRequest;
import com.wj.future.campus.entity.request.SearchConversationRequest;
import com.wj.future.campus.entity.response.ConversationMessageResponse;
import com.wj.future.campus.entity.response.HistoryResponse;
import com.wj.future.campus.entity.response.SearchConversationResponse;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.mapper.AiToUserConversationHistoryMapper;
import com.wj.future.campus.producer.RabbitMQProducer;
import com.wj.future.campus.result.R;
import com.wj.future.campus.service.AiStreamService;
import com.wj.future.campus.service.AiToUserConversationService;
import com.wj.future.campus.util.DashScopeGenerationImageUtil;
import com.wj.future.campus.util.MinioUtil;
import com.wj.future.campus.util.UserUtil;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private DashScopeGenerationImageUtil generationImageUtil;

    @Autowired
    private ImageModel imageModel;

    @Autowired
    private AiToUserConversationHistoryMapper aiToUserConversationHistoryMapper;


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

    /**
     * 获取历史对话消息
     * @param historyConversationRequest 查询参数
     * @param request 请求对象
     * @return 历史消息列表
     */
    @GetMapping("/getHistoryConversation")
    @ApiOperationSupport(order = 2, author = "wj")
    public R<Page<HistoryResponse>> getHistoryConversation(HistoryConversationRequest historyConversationRequest, HttpServletRequest request) {

        UserPojo userPojo;
        try {
            userPojo = userUtil.getUser(request);
        } catch (FormWallException e) {
            logger.info("用户未登录，暂时历史对话消息");
            return R.ok(new Page<>(1, 10,0));
        }
        LambdaQueryWrapper<AiToUserConversationHistoryPojo> qw = new LambdaQueryWrapper<>();
        qw.eq(AiToUserConversationHistoryPojo::getUserId,userPojo.getId());
        qw.orderByDesc(AiToUserConversationHistoryPojo::getCreateTime);
        Page<AiToUserConversationHistoryPojo> page = new Page<>(historyConversationRequest.getPageNum(), historyConversationRequest.getPageSize());
        Page<AiToUserConversationHistoryPojo> aiToUserConversationHistoryPojoPage = aiToUserConversationHistoryMapper.selectPage(page, qw);

        Page<HistoryResponse> historyResponsePage = new Page<>(historyConversationRequest.getPageNum(), historyConversationRequest.getPageSize(), aiToUserConversationHistoryPojoPage.getTotal());

        List<AiToUserConversationHistoryPojo> records = aiToUserConversationHistoryPojoPage.getRecords();

        if (CollUtil.isNotEmpty(records)){

            List<HistoryResponse> historyResponses = records.stream().map(historyResponse ->
                    new HistoryResponse(historyResponse.getTitle(), historyResponse.getConversationId())
            ).collect(Collectors.toList());

            historyResponsePage.setRecords(historyResponses);
        }
        return R.ok(historyResponsePage);
    }

    @GetMapping("/searchConversation")
    @AuthIsLogin
    @ApiOperationSupport(order = 3, author = "wj")
    public R<Page<SearchConversationResponse>> searchConversation(SearchConversationRequest searchConversationRequest, HttpServletRequest request) throws FormWallException {
        UserPojo user = userUtil.getUser(request);

        Page<SearchConversationResponse> responsePage = aiToUserConversationService.searchConversation(searchConversationRequest,user);

        return R.ok(responsePage);
    }

    ///  获取对话列表
    @GetMapping("/getConversationMessage")
    @AuthIsLogin
    public R<Page<ConversationMessageResponse>> getConversationMessage(@RequestParam String conversationId, @RequestParam int pageNum, @RequestParam int pageSize){
        Page<AiToUserConversationPoJo> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<AiToUserConversationPoJo> qw = new LambdaQueryWrapper<>();
        qw.eq(AiToUserConversationPoJo::getConversationId,conversationId);
        Page<AiToUserConversationPoJo> pageResult = aiToUserConversationService.page(page, qw);

        Page<ConversationMessageResponse> userToBotConversationPage = new Page<>(pageNum, pageSize, pageResult.getTotal());
        if (CollUtil.isNotEmpty(pageResult.getRecords())){
            List<AiToUserConversationPoJo> records = pageResult.getRecords();
            List<ConversationMessageResponse> conversationMessageResponses = new ArrayList<>();
            for (AiToUserConversationPoJo record : records) {

                ConversationMessageResponse user = new ConversationMessageResponse();
                user.setRole("user");
                user.setContent(record.getUser());
                conversationMessageResponses.add(user);

                ConversationMessageResponse bot = new ConversationMessageResponse();
                bot.setRole("ai");
                bot.setContent(record.getBot());
                conversationMessageResponses.add(bot);


            }


            userToBotConversationPage.setRecords(conversationMessageResponses);
        }
        return R.ok(userToBotConversationPage);
    }


    /**
     * 文档结构化
     * @param file 文件流
     * @return 是否成功
     */
    @PostMapping("/documentStructuring")
    @AuthIsLogin
    @ApiOperationSupport(order = 4, author = "wj")
    public R<String> documentStructuring(@RequestPart("file")MultipartFile file){
        // 判断文件格式
        String fileName = file.getOriginalFilename();
        if (StrUtil.isBlank(fileName)){
            return R.failure("请上传文件");
        }
        if (!".pdf".endsWith(fileName) && !".doc".endsWith(fileName)) {
            return R.failure("请上传pdf或者doc格式的文件");
        }
        String originalFilename = file.getOriginalFilename();
        //
        String objectName = minioUtil.getObjectName(originalFilename);
        minioUtil.upload(file, objectName);

        //ai解析文档


        return R.ok();
    }
    /// 生成图片（使用 LangChain4j）
    @PostMapping("/generateImage")
    @AuthIsLogin
    public R<String> generateImage(@RequestBody AiChatRequest aiChatRequest){
        String msg = aiChatRequest.getMsg();
        Response<Image> generate = imageModel.generate(msg);
        return R.ok(generate.content().base64Data());
    }

    /// 生成图片（使用阿里云通义万相）
    @PostMapping("/generateImageWithAliyun")
    @ApiOperationSupport(order = 5, author = "wj")
    public R<String> generateImageWithAliyun(@RequestBody AiChatRequest aiChatRequest){
        try {
            String msg = aiChatRequest.getMsg();
            if (StrUtil.isBlank(msg)) {
                return R.failure("请输入图片描述");
            }
            // 调用阿里云通义万相生成图片
            String imageUrl = generationImageUtil.generationImage(msg);
            return R.ok(imageUrl);
        } catch (Exception e) {
            logger.error("生成图片失败", e);
            return R.failure("生成图片失败: " + e.getMessage());
        }
    }
}
