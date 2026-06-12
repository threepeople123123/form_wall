//package com.future.campus.util;
//
//import dev.langchain4j.data.embedding.Embedding;
//import dev.langchain4j.model.embedding.EmbeddingModel;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * Embedding 工具类
// * 用于将文本转换为向量
// */
//@Slf4j
//@Component
//public class EmbeddingUtil {
//
////    @Autowired
////    private EmbeddingModel embeddingModel;
////
////    /**
////     * 将文本转换为向量（List<Double> 格式）
////     *
////     * @param text 输入文本
////     * @return 向量列表
////     */
////    public List<Double> embedToVector(String text) {
////        try {
////            log.debug("开始向量化文本: {}", text.substring(0, Math.min(50, text.length())));
////
////            Embedding embedding = embeddingModel.embed(text).content();
////            // LangChain4j 返回的是 List<Float>，需要转换为 List<Double>
////            List<Float> floatVector = embedding.vectorAsList();
////            List<Double> vector = floatVector.stream()
////                .map(Float::doubleValue)
////                .toList();
////
////            log.debug("向量化完成，维度: {}", vector.size());
////            return vector;
////        } catch (Exception e) {
////            log.error("向量化失败", e);
////            throw new RuntimeException("文本向量化失败", e);
////        }
////    }
//
//    /**
//     * 将文本转换为向量数组（float[] 格式，适用于 PostgreSQL pgvector）
//     *
//     * @param text 输入文本
//     * @return 浮点数组
//     */
//    public float[] embedToArray(String text) {
//        List<Double> vector = embedToVector(text);
//
//        // 转换为 float 数组
//        float[] floatArray = new float[vector.size()];
//        for (int i = 0; i < vector.size(); i++) {
//            floatArray[i] = vector.get(i).floatValue();
//        }
//
//        return floatArray;
//    }
//
//    /**
//     * 批量向量化
//     *
//     * @param texts 文本列表
//     * @return 向量列表
//     */
//    public List<List<Double>> batchEmbed(List<String> texts) {
//        return texts.stream()
//            .map(this::embedToVector)
//            .toList();
//    }
//
//    /**
//     * 计算两个向量的余弦相似度
//     *
//     * @param vector1 向量1
//     * @param vector2 向量2
//     * @return 相似度（0-1之间，越接近1表示越相似）
//     */
//    public double cosineSimilarity(List<Double> vector1, List<Double> vector2) {
//        if (vector1.size() != vector2.size()) {
//            throw new IllegalArgumentException("向量维度不一致");
//        }
//
//        double dotProduct = 0.0;
//        double norm1 = 0.0;
//        double norm2 = 0.0;
//
//        for (int i = 0; i < vector1.size(); i++) {
//            dotProduct += vector1.get(i) * vector2.get(i);
//            norm1 += vector1.get(i) * vector1.get(i);
//            norm2 += vector2.get(i) * vector2.get(i);
//        }
//
//        if (norm1 == 0 || norm2 == 0) {
//            return 0.0;
//        }
//
//        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
//    }
//}
