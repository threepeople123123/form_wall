package com.wj.future.campus.rag;

import com.wj.future.campus.entity.pojo.ConversationVectorPojo;
import com.wj.future.campus.service.ConversationVectorService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 基于 PostgreSQL pgvector 的内容检索器
 * 用于从历史对话中检索相似内容
 */
@Slf4j
@Component
public class PgVectorContentRetriever implements ContentRetriever {

    public static ThreadLocal<AtomicReference<String>> threadLocal = new ThreadLocal<>();


    public void setAtomicReference(String schoolId) {
        AtomicReference<String> stringAtomicReference = threadLocal.get();
        stringAtomicReference.set(schoolId);
    }


    public String getAtomicReference() {
        AtomicReference<String> stringAtomicReference = threadLocal.get();
        return stringAtomicReference.get();
    }

    @Autowired
    private ConversationVectorService conversationVectorService;

    @Autowired
    private EmbeddingModel embeddingModel;

    private final int maxResults;
    private final double minScore;

    public PgVectorContentRetriever() {
        this.maxResults = 5;  // 默认返回最相似的5条
        this.minScore = 0.8;  // 最低相似度阈值
    }

    public PgVectorContentRetriever(int maxResults, double minScore) {
        this.maxResults = maxResults;
        this.minScore = minScore;
    }

    @Override
    public List<Content> retrieve(Query query) {
        try {
            String queryText = query.text();
            log.info("开始向量检索，查询文本: {}", queryText);

            // 1. 将查询文本转换为向量
            Embedding queryEmbedding = embeddingModel.embed(queryText).content();
            // LangChain4j 返回的是 List<Float>，需要转换为 List<Double>
            List<Float> floatVector = queryEmbedding.vectorAsList();
            List<Double> vectorList = floatVector.stream()
                    .map(Float::doubleValue)
                    .toList();

            // 2. 使用 PostgreSQL pgvector 进行相似度搜索
            List<ConversationVectorPojo> similarConversations =
                    conversationVectorService.findMostSimilarByUserVector(vectorList, maxResults);

            if (similarConversations == null || similarConversations.isEmpty()) {
                log.info("未找到相似的对话记录");
                return new ArrayList<>();
            }

            // 3. 将检索结果转换为 LangChain4j 的 Content 格式
            List<Content> contents = similarConversations.stream()
                    .map(conv -> {
                        // 构建上下文内容：用户问题 + AI回答
                        String context = String.format(
                                "用户问题: %s\nAI回答: %s",
                                conv.getUserMsg(),
                                conv.getBotMsg()
                        );

                        TextSegment segment = TextSegment.from(context);
                        return Content.from(segment);
                    })
                    .collect(Collectors.toList());

            log.info("检索到 {} 条相似对话", contents.size());
            return contents;

        } catch (Exception e) {
            log.error("向量检索失败", e);
            return new ArrayList<>();
        }
    }
}
