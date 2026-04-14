package com.wj.future.campus.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import com.alibaba.dashscope.common.History;
import com.wj.future.campus.entity.nosql.UserToBotConversation;
import com.wj.future.campus.entity.request.AiChatRequest;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.wj.future.campus.campusEnum.RedisEnum.USER_BOT_TO_CONVERSATION;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource(name = "zhiPuServiceImpl")
    private AiService zhiPuService;

    @Resource(name = "qianWenServiceImpl")
    private AiService qianWenService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody AiChatRequest aiChatRequest) throws IOException, FormWallException {
        String msg = aiChatRequest.getMsg();
        String conversationId = aiChatRequest.getConversationId();
        if (StrUtil.isBlank(msg) && StrUtil.isBlank(conversationId)) {
            throw new FormWallException("请输入内容");
        }
        //取出历史对话信息
        String redisHistory = (String)redisTemplate.opsForHash().get(USER_BOT_TO_CONVERSATION.getKey(),conversationId);
        List<History> histories = new ArrayList<>();
        if (StrUtil.isNotBlank(redisHistory)){
            List<UserToBotConversation> userToBotConversations = JSONUtil.toList(redisHistory, UserToBotConversation.class);
            userToBotConversations = userToBotConversations.subList(Math.max(userToBotConversations.size() - 10, 0), userToBotConversations.size());
            for (UserToBotConversation userToBotConversation : userToBotConversations) {
                History history = History.builder().bot(userToBotConversation.getBot()).user(userToBotConversation.getUser()).build();
                histories.add(history);
            }
        }


        // 用户问题向量化
        List<Double> embedding = qianWenService.embedding(msg);

        List<Float> embeddingToFloat = embedding.stream().map(item -> Float.valueOf(String.valueOf(item))).toList();

        // 强制构建一个单一的 KnnQuery 对象，而不是使用 lambda 列表
        KnnQuery knnQuery = new KnnQuery.Builder()
                .field("embedding")
                .queryVector(embeddingToFloat)
                .k(5)
                .numCandidates(100)
                .build();

//        SearchResponse<KnowledgeDoc> response = elasticsearchClient.search(s -> s
//                        .index("ai_chat_knowledge")
//                        .knn(knnQuery), // 这里传入单个对象，看是否触发 Client 的兼容逻辑
//                KnowledgeDoc.class
//        );
//        List<KnowledgeDoc> knowledgeDocs = response.hits().hits().stream().map(Hit::source).toList();
        String knowledgeDoc = "";
//        if (CollUtil.isNotEmpty(knowledgeDocs)) {
//            knowledgeDoc = knowledgeDocs.stream().map(KnowledgeDoc::getContent).collect(Collectors.joining(","));
//        }

        // 使用流式调用方法
        return qianWenService.chatForStream(msg,histories, knowledgeDoc,conversationId);
    }
}
