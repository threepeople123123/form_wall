package com.wj.future.campus.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置类
 */
@Configuration
public class LangChain4jConfig {

    /**
     * 配置嵌入模型 (使用本地模型 All-MiniLM-L6-v2)
     * 这是一个轻量级的本地嵌入模型，适合中文和英文文本向量化
     * 
     * @return 嵌入模型实例
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }
}
