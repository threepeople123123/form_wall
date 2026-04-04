package com.wj.future.compus.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 LangChain4j 的向量化服务
 * 用于对话内容的向量化处理和相似度搜索
 */
@Service
public class LangChain4jEmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    // 内存中的向量存储 (生产环境建议使用 Redis、Elasticsearch 等)
    private final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    /**
     * 将文本转换为向量
     * 
     * @param text 输入文本
     * @return 向量表示
     */
    public List<Double> embedText(String text) {
        Embedding embedding = embeddingModel.embed(text).content();
        // 将 Float 列表转换为 Double 列表
        return embedding.vectorAsList().stream()
                .map(Double::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 批量添加对话到向量库
     * 
     * @param texts 文本列表
     * @param ids   对应的 ID 列表
     */
    public void addConversations(List<String> texts, List<String> ids) {
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            String id = ids.get(i);
            TextSegment segment = TextSegment.from(text);
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add( embedding, segment);
        }
    }

    /**
     * 搜索最相似的对话
     * 
     * @param query 查询文本
     * @param maxResults 最大返回结果数
     * @return 匹配的对话列表
     */
    public List<EmbeddingMatch<TextSegment>> searchSimilarConversations(String query, int maxResults) {
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(query).content())
                .maxResults(maxResults)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        return result.matches();
    }

    /**
     * 计算两个文本的相似度
     * 
     * @param text1 第一个文本
     * @param text2 第二个文本
     * @return 相似度分数 (0-1 之间，越接近 1 越相似)
     */
    public double cosineSimilarity(String text1, String text2) {
        Embedding embedding1 = embeddingModel.embed(text1).content();
        Embedding embedding2 = embeddingModel.embed(text2).content();
        
        return cosineSimilarity(embedding1, embedding2);
    }

    /**
     * 计算两个向量的余弦相似度
     * 
     * @param embedding1 第一个向量
     * @param embedding2 第二个向量
     * @return 相似度分数 (0-1 之间)
     */
    private double cosineSimilarity(Embedding embedding1, Embedding embedding2) {
        List<Float> vector1 = embedding1.vectorAsList();
        List<Float> vector2 = embedding2.vectorAsList();

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            float v1 = vector1.get(i);
            float v2 = vector2.get(i);
            dotProduct += v1 * v2;
            norm1 += Math.pow(v1, 2);
            norm2 += Math.pow(v2, 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 获取向量维度
     * 
     * @return 向量的维度
     */
    public int getDimension() {
        Embedding embedding = embeddingModel.embed("test").content();
        return embedding.vector().length;
    }
}
